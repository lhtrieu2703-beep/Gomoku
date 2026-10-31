"""Read-only independent replay/statistics checks; Python standard library only.

Does not call Board.checkWin, MinimaxAI, or the arena's aggregation code.
Run after the arena completes: python tools/ai/verify-ai-results.py
"""
import argparse
import csv
import math
import re
import statistics
from collections import Counter, defaultdict
from pathlib import Path


def require(condition, message):
    if not condition:
        raise AssertionError(message)


def rows(path):
    with path.open(encoding="utf-8", newline="") as source:
        return list(csv.DictReader(source))


def wins(grid, row, col):
    piece = grid[row][col]
    if piece == ".":
        return False
    opponent = "O" if piece == "X" else "X"
    for dr, dc in ((0, 1), (1, 0), (1, 1), (1, -1)):
        line = 1
        blocked = 0
        for sign in (-1, 1):
            r, c = row + sign * dr, col + sign * dc
            while 0 <= r < 15 and 0 <= c < 15 and grid[r][c] == piece:
                line += 1
                r, c = r + sign * dr, c + sign * dc
            blocked += int(0 <= r < 15 and 0 <= c < 15 and grid[r][c] == opponent)
        if line >= 5 and blocked < 2:
            return True
    return False


def close(actual, expected, context):
    require(math.isfinite(actual) and abs(actual - expected) <= 0.000002,
            f"{context}: {actual} != {expected}")


def markdown_rows(path):
    return [[cell.strip() for cell in line.strip().strip("|").split("|")]
            for line in path.read_text(encoding="utf-8").splitlines() if line.startswith("| ")]


def check_arena(output):
    games = rows(output / "arena-games.csv")
    traces = defaultdict(list)
    for move in rows(output / "arena-moves.csv"):
        traces[move["game_id"]].append(move)
    require(len({g["game_id"] for g in games}) == len(games), "Duplicate game id")
    require(set(traces) == {g["game_id"] for g in games}, "Missing/extra move trace")
    by_pair = defaultdict(list)
    example = None
    for game in games:
        gid = game["game_id"]
        by_pair[game["pair"]].append(game)
        trace = traces[gid]
        grid = [["."] * 15 for _ in range(15)]
        require(1 <= len(trace) == int(game["moves"]) <= 225, f"{gid}: move count")
        require(not game["anomaly"], f"{gid}: recorded anomaly {game['anomaly']}")
        require(game["x_actor"] == game["first_actor"] and game["o_actor"] != game["x_actor"], f"{gid}: color mapping")
        times = defaultdict(list)
        for ply, move in enumerate(trace, 1):
            require(int(move["ply"]) == ply, f"{gid}: nonconsecutive ply")
            color = "X" if ply % 2 else "O"
            actor = game["x_actor"] if color == "X" else game["o_actor"]
            require(move["color"] == color and move["actor"] == actor, f"{gid}: wrong actor/color")
            r, c = int(move["row"]), int(move["col"])
            require(0 <= r < 15 and 0 <= c < 15 and grid[r][c] == ".", f"{gid}: illegal placement")
            if ply == len(trace) and game["pair"] == "MEDIUM-HARD" and game["result"] == "A_WIN" and example is None:
                example = (gid, [row.copy() for row in grid], (r, c), color)
            grid[r][c] = color
            won = wins(grid, r, c)
            require(not won or ply == len(trace), f"{gid}: continued after terminal win")
            if ply == len(trace):
                expected = actor + "_WIN" if won else "DRAW" if ply == 225 else "NONTERMINAL"
                require(game["result"] == expected, f"{gid}: wrong outcome {expected}")
            value = float(move["elapsed_ms"])
            require(math.isfinite(value) and value >= 0, f"{gid}: invalid timing")
            require(int(move["minimax_nodes"]) >= 0 and int(move["alpha_beta_prunes"]) >= 0, f"{gid}: negative counters")
            times[actor].append(value)
        for actor in ("A", "B"):
            prefix = actor.lower()
            values = times[actor]
            require(int(game[prefix + "_moves"]) == len(values), f"{gid}: {actor} count")
            close(float(game[prefix + "_mean_ms"]), statistics.mean(values), f"{gid}: {actor} mean")
            close(float(game[prefix + "_max_ms"]), max(values), f"{gid}: {actor} max")
        require(float(game["total_ms"]) + .001 >= sum(sum(v) for v in times.values()), f"{gid}: total time")
    expected_pairs = {"EASY-MEDIUM", "EASY-HARD", "MEDIUM-HARD", "EASY-EASY", "MEDIUM-MEDIUM", "HARD-HARD"}
    require(set(by_pair) == expected_pairs, "Missing/extra pairs")
    summary = {r[0]: r for r in markdown_rows(output / "arena-summary.md") if r[0] in expected_pairs}
    for pair, group in by_pair.items():
        n = len(group)
        require(n in (30, 50), f"{pair}: expected 30 or 50 games")
        require(Counter(g["first_actor"] for g in group) == {"A": n // 2, "B": n // 2}, f"{pair}: unbalanced starters")
        counts = Counter(g["result"] for g in group)
        aw, draw, bw = counts["A_WIN"], counts["DRAW"], counts["B_WIN"]
        xw = sum(g["result"] == g["first_actor"] + "_WIN" for g in group)
        require([int(v) for v in summary[pair][1:6]] == [n, aw, draw, bw, 0], f"{pair}: summary counts")
        for index, count in ((6, aw), (7, draw), (8, bw)):
            close(float(summary[pair][index]), count * 100 / n, f"{pair}: percentage")
        require([int(v) for v in summary[pair][9:11]] == [xw, aw + bw - xw], f"{pair}: X/O summary")
    print(f"ARENA VERIFIED: {len(games)} games; {sum(len(t) for t in traces.values())} legal placements; balanced starters; RULE_B outcomes; timing aggregates; summary percentages")
    if example:
        gid, grid, move, color = example
        print(f"OBSERVATION {gid}: board BEFORE final {color} move {move}; observed outcome, not a tactical expectation")
        print("    012345678901234")
        for r, row in enumerate(grid):
            print(f"{r:02d}  {''.join(row)}")


def check_benchmark(output):
    samples = rows(output / "benchmark.csv")
    require(len(samples) == 210, "Expected 60 warmup + 150 measured benchmark rows")
    groups = defaultdict(list)
    for sample in samples:
        groups[(sample["position"], sample["level"])].append(sample)
        for field in ("elapsed_ms", "minimax_nodes", "alpha_beta_prunes"):
            value = float(sample[field])
            require(math.isfinite(value) and value >= 0, f"Invalid benchmark {field}")
    require(len(groups) == 30, "Expected ten positions x three levels")
    positions = {}
    text = (output / "benchmark-positions.md").read_text(encoding="utf-8")
    for pid, stones, turn, board_text in re.findall(r"## ([\w-]+); stones=(\d+); turn=([XO])\n.*?```text\n(.*?)```", text, re.S):
        grid = [list(line[4:]) for line in board_text.splitlines()[1:]]
        require(len(grid) == 15 and all(len(row) == 15 for row in grid), f"{pid}: board shape")
        require(sum(cell != "." for row in grid for cell in row) == int(stones), f"{pid}: stone count")
        require(all(not wins(grid, r, c) for r in range(15) for c in range(15)), f"{pid}: pre-existing win")
        positions[pid] = (grid, int(stones), turn)
    require(len(positions) == 10, "Missing ASCII positions")
    summaries = {r[0]: r for r in markdown_rows(output / "benchmark-summary.md") if r[0] != "Level" and r[0] != "Position / level"}
    for (pid, level), group in groups.items():
        require(Counter(s["phase"] for s in group) == {"warmup": 2, "measured": 5}, f"{pid}/{level}: repetition count")
        require(sorted(int(s["repetition"]) for s in group if s["phase"] == "measured") == [1, 2, 3, 4, 5], f"{pid}/{level}: repetitions")
        grid, stones, turn = positions[pid]
        for sample in group:
            require(int(sample["stones"]) == stones and sample["side_to_move"] == turn, f"{pid}: metadata")
            r, c = int(sample["row"]), int(sample["col"])
            require(0 <= r < 15 and 0 <= c < 15 and grid[r][c] == ".", f"{pid}: illegal benchmark move")
    measured = [s for s in samples if s["phase"] == "measured"]
    for name, table in summaries.items():
        if " / " in name:
            pid, level = name.split(" / ")
            selected = [s for s in measured if s["position"] == pid and s["level"] == level]
        else:
            selected = [s for s in measured if s["level"] == name]
        require(int(table[1]) == len(selected), f"{name}: summary n")
        for col, field in enumerate(("elapsed_ms", "minimax_nodes", "alpha_beta_prunes"), 2):
            actual_median, actual_sd = map(float, table[col].split(" ± "))
            values = [float(s[field]) for s in selected]
            close(actual_median, statistics.median(values), f"{name}: {field} median")
            close(actual_sd, statistics.pstdev(values), f"{name}: {field} population SD")
    require(len(summaries) == 33, "Expected 30 per-position and three pooled summaries")
    print("BENCHMARK VERIFIED: 10 nonterminal ASCII positions; 60 warm-ups + 150 measured samples; legal choices; 33 median/SD summary rows")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=Path(__file__).resolve().parents[2] / "docs" / "ai-results")
    arguments = parser.parse_args()
    check_arena(arguments.output)
    check_benchmark(arguments.output)
