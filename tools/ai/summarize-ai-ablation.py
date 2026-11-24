"""Validate paired ablation CSVs and derive Markdown; Python standard library only."""
import argparse
import csv
import math
import re
import statistics
from collections import defaultdict
from pathlib import Path


def number(value):
    return "NA" if value is None else f"{value:.6f}"


def ratio(off, on):
    return off / on if on else None


def median(values):
    values = [value for value in values if value is not None]
    return statistics.median(values) if values else None


def timings(rows):
    values = sorted(float(row["elapsed_ms"]) for row in rows)
    return "/".join(number(value) for value in (
        statistics.median(values), values[math.ceil(0.95 * len(values)) - 1], values[-1]))


def corpus(path):
    pattern = r"^## ([^;\n]+); stones=(\d+); turn=([XO])\n.*?```text\n(.*?)```"
    return {match[0]: match[1:] for match in re.findall(
        pattern, path.read_text(encoding="utf-8"), flags=re.MULTILINE | re.DOTALL)}


def summarize(output):
    environment = (output / "environment.md").read_text(encoding="utf-8")
    protocol = re.search(r"Warmups per position/level/pruning: (\d+); measurements: (\d+)", environment)
    if protocol is None:
        raise ValueError("Missing warmup/measurement protocol")
    warmups, measurements = map(int, protocol.groups())
    mode = re.search(r"^- Mode: (.+)$", environment, re.MULTILINE).group(1)
    timestamp = re.search(r"^- Timestamp \(Asia/Saigon\): (.+)$", environment, re.MULTILINE).group(1)
    with (output / "ablation-raw.csv").open(encoding="utf-8", newline="") as stream:
        rows = list(csv.DictReader(stream))
    anomalies, mismatches = [], []
    original = corpus(Path(__file__).resolve().parents[2] / "docs/ai-results/benchmark-positions.md")
    actual = corpus(output / "ablation-positions.md")
    if not actual or any(original.get(key) != value for key, value in actual.items()):
        anomalies.append("Corpus khác benchmark gốc (ô cờ/số quân/bên đi).")
    expected = len(actual) * 2 * 2 * (warmups + measurements)
    if len(rows) != expected:
        anomalies.append(f"CSV chưa đủ: {len(rows)}/{expected} dòng.")
    if "- Run status: completed" not in environment:
        anomalies.append("Runner không ghi trạng thái completed.")
    groups, paired = defaultdict(list), defaultdict(dict)
    seen = set()
    for index, row in enumerate(rows):
        key = (row["position"], row["level"], row["phase"], int(row["repetition"]))
        identity = (*key, row["pruning"])
        if identity in seen:
            anomalies.append(f"Dòng trùng: {identity}")
        seen.add(identity)
        if row["position"] not in actual or row["level"] not in {"MEDIUM", "HARD"}:
            anomalies.append(f"Thế/mức không hợp lệ: {identity}")
        if row["mode"] != mode or row["depth"] != {"MEDIUM": "2", "HARD": "3"}.get(row["level"]):
            anomalies.append(f"Mode/depth không hợp lệ: {identity}")
        count = {"warmup": warmups, "measure": measurements}.get(row["phase"], 0)
        if not 1 <= key[3] <= count:
            anomalies.append(f"Phase/repetition không hợp lệ: {identity}")
        if row["pruning"] != ("on" if index % 2 == 0 else "off"):
            anomalies.append(f"Sai thứ tự xen kẽ tại dòng {index + 2}.")
        elif index % 2 and key != (rows[index - 1]["position"], rows[index - 1]["level"],
                                  rows[index - 1]["phase"], int(rows[index - 1]["repetition"])):
            anomalies.append(f"Cặp on/off không cùng thế/mức/lượt tại dòng {index + 2}.")
        elapsed = float(row["elapsed_ms"])
        if not math.isfinite(elapsed) or elapsed < 0:
            anomalies.append(f"Thời gian không hợp lệ: {identity}")
        if row["status"] != "ok":
            anomalies.append(f"{row['status']}: {identity}, {row['elapsed_ms']} ms")
            continue
        nodes, prunes = int(row["minimax_nodes"]), int(row["alpha_beta_prunes"])
        if nodes < 0 or prunes < 0 or (row["pruning"] == "off" and prunes != 0):
            anomalies.append(f"Số nút/prune không hợp lệ: {identity}")
        if mode == "search-only" and nodes > 0 and not row["root_value"]:
            anomalies.append(f"Thiếu root_value: {identity}")
        groups[(row["position"], row["level"], row["pruning"])].append(row)
        paired[key][row["pruning"]] = row

    for position in actual:
        for level in ("MEDIUM", "HARD"):
            for pruning in ("on", "off"):
                samples = groups[(position, level, pruning)]
                if len(samples) != warmups + measurements:
                    anomalies.append(f"Thiếu mẫu ok: {position}/{level}/{pruning}.")
                signature = {(r["minimax_nodes"], r["alpha_beta_prunes"], r["best_row"],
                              r["best_col"], r["root_value"]) for r in samples}
                if len(signature) > 1:
                    anomalies.append(f"Kết quả/số nút không xác định: {position}/{level}/{pruning}: {sorted(signature)}")

    move_differences = value_differences = comparisons = 0
    for key, pair in paired.items():
        if set(pair) != {"on", "off"}:
            anomalies.append(f"Thiếu cặp: {key}")
            continue
        on, off = pair["on"], pair["off"]
        if int(off["minimax_nodes"]) < int(on["minimax_nodes"]):
            anomalies.append(f"OFF có ít nút hơn ON: {key}")
        move_diff = (on["best_row"], on["best_col"]) != (off["best_row"], off["best_col"])
        value_diff = on["root_value"] != off["root_value"]
        if key[2] == "measure":
            comparisons += 1
            move_differences += move_diff
            value_differences += value_diff
        if move_diff or value_diff:
            mismatches.append((key, on, off))

    text = ["# Thí nghiệm ablation Alpha-Beta", "", f"Lần chạy MỚI: {timestamp}; mode `{mode}`. "
            "Không so sánh hiệu năng với số liệu ngày 31/10/2026.", "",
            f"Corpus: {len(actual)} thế từ `AIArena.corpus()`, RULE_B; "
            f"MEDIUM 2 ply (16/10 ứng viên), HARD 3 ply (22/14 ứng viên). "
            f"Mỗi (thế, mức, cờ): {warmups} lượt làm nóng, {measurements} lượt đo; "
            "mỗi lượt chạy ON rồi OFF, tuần tự trong cùng JVM. Bỏ warmup khi tổng hợp.", "",
            "Thời gian đo bằng nanoTime, gồm sinh/sắp xếp ứng viên và lời gọi tìm kiếm trực tiếp; "
            "không gồm sao chép bàn, cấu hình, tạo watchdog, kiểm tra bất biến hay ghi file. "
            "p95 dùng nearest-rank ceil(0.95*n): với n=20 là mẫu thứ 19 sau sắp xếp. "
            "Số thập phân trong bảng được tính từ CSV, hiển thị 6 chữ số.", "",
            "## Theo thế và mức", "",
            "Mỗi ô thời gian là trung vị/p95/max (ms). Số nút và prune là số xác định của một lời gọi "
            "(đã kiểm tra ổn định qua cả warmup và đo). Tỉ lệ đều là OFF/ON; NA nếu mẫu số bằng 0.", "",
            "| Thế | Mức | n ON/OFF | ON ms (median/p95/max) | OFF ms (median/p95/max) | Nút ON/OFF | Tỉ lệ nút | Tỉ lệ thời gian (median) | Prune ON |",
            "|---|---|---:|---:|---:|---:|---:|---:|---:|"]
    totals = defaultdict(list)
    zero_groups = []
    for position in actual:
        for level in ("MEDIUM", "HARD"):
            on = [r for r in groups[(position, level, "on")] if r["phase"] == "measure"]
            off = [r for r in groups[(position, level, "off")] if r["phase"] == "measure"]
            if not on or not off:
                continue
            on_nodes, off_nodes = int(on[0]["minimax_nodes"]), int(off[0]["minimax_nodes"])
            node_ratio = ratio(off_nodes, on_nodes)
            time_ratio = ratio(statistics.median(float(r["elapsed_ms"]) for r in off),
                               statistics.median(float(r["elapsed_ms"]) for r in on))
            prunes = on[0]["alpha_beta_prunes"]
            text.append(f"| {position} | {level} | {len(on)}/{len(off)} | {timings(on)} | {timings(off)} "
                        f"| {on_nodes}/{off_nodes} | {number(node_ratio)} | {number(time_ratio)} | {prunes} |")
            totals[level].append((on_nodes, off_nodes, sum(int(r["minimax_nodes"]) for r in on),
                                  sum(int(r["minimax_nodes"]) for r in off), node_ratio, time_ratio))
            if on_nodes == 0 or off_nodes == 0:
                zero_groups.append(f"{position}/{level} (ON={on_nodes}, OFF={off_nodes})")

    text += ["", "## Tổng hợp theo mức", "",
             "Tổng 1 lượt/thế cộng số nút của từng thế một lần. Tổng đo cộng tất cả lượt measure "
             "(không cộng warmup). Trung vị tỉ lệ được lấy theo thế, không gộp mẫu thời gian của các thế.", "",
             "| Mức | Tổng nút 1 lượt/thế ON/OFF | Tổng nút các lượt đo ON/OFF | Trung vị tỉ lệ nút | Trung vị tỉ lệ thời gian | Giảm nút (%) |",
             "|---|---:|---:|---:|---:|---:|"]
    for level, samples in totals.items():
        on, off, all_on, all_off = (sum(row[i] for row in samples) for i in range(4))
        reduction = (1 - on / off) * 100 if off else None
        text.append(f"| {level} | {on}/{off} | {all_on}/{all_off} | {number(median(row[4] for row in samples))} "
                    f"| {number(median(row[5] for row in samples))} | {number(reduction)} |")

    measured_mismatches = sum(key[2] == "measure" for key, _, _ in mismatches)
    text += ["", "## Kiểm tra tính đúng đắn", "",
             f"- Đã so sánh {comparisons} cặp lượt đo ON/OFF; {measured_mismatches} cặp khác nhau "
             f"(khác nước: {move_differences}; khác root_value: {value_differences}).",
             f"- Khác nhau khi làm nóng: {sum(key[2] == 'warmup' for key, _, _ in mismatches)} cặp.",
             f"- CSV có {len(rows)} dòng: {sum(r['phase'] == 'warmup' for r in rows)} warmup, "
             f"{sum(r['phase'] == 'measure' for r in rows)} measure; "
             f"timeout: {sum(r['status'] == 'timeout' for r in rows)}.",
             "- Đã kiểm tra corpus từng ô/số quân/bên đi với benchmark-positions.md gốc; "
             "đủ lượt, thứ tự xen kẽ, độ sâu, tính ổn định số nút/nước/giá trị; OFF prune=0 và OFF nodes>=ON nodes.",
             "- Runner kiểm tra bất biến từng ô/số quân/luật sau mỗi lời gọi; chỉ ghi mẫu ok khi nước trả về hợp lệ.",
             "- root_value là giá trị Minimax gốc, không phải Move.score. Ở full, ô trống nghĩa là "
             "tiền xử lý trả về trước Minimax; hai ô trống chỉ xác nhận cùng không chạy tìm kiếm gốc."]
    if mismatches:
        text += ["", "### Chi tiết khác nhau", "", "| Thế/mức/phase/lượt | ON row,col,value | OFF row,col,value |", "|---|---|---|"]
        for key, on, off in mismatches:
            text.append(f"| {'/'.join(map(str, key))} | {on['best_row']},{on['best_col']},{on['root_value']} "
                        f"| {off['best_row']},{off['best_col']},{off['root_value']} |")
        text += ["", "Cần điều tra tie-breaking/cờ cắt tỉa hoặc giá trị trả về trước khi dùng kết luận hiệu năng."]
    text += ["", "## Giới hạn và điểm cần lưu ý", "",
             "- Số nút là đại lượng xác định cho code/corpus/cấu hình này. Thời gian có nhiễu do JIT, GC, "
             "lịch CPU, nhiệt độ và tải nền. Không suy ra tốc độ chỉ từ tỉ lệ nút; chưa đo khoảng tin cậy thống kê.",
             "- Hai cấu hình giữ nguyên sinh/sắp xếp/giới hạn ứng viên, lượng giá, độ sâu, dấu > khi chọn nước gốc "
             "và try/finally rút quân. Cờ chỉ vô hiệu hóa các cutoff beta<=alpha. "
             "Các return khi đã tìm thấy thắng trong đệ quy vẫn giữ nguyên: đây là so sánh kernel hiện tại, "
             "không phải Minimax duyệt toàn bộ ô hợp lệ của Gomoku.",
             "- search-only bỏ các shortcut chiến thuật và dùng cùng kernel gốc; nước của nó có thể khác full. "
             "Các thế late-40/60/100 là tập con tổng hợp, không đại diện mọi tàn cuộc thực tế."]
    if zero_groups:
        text += [f"- Các nhóm có minimax_nodes=0: {'; '.join(zero_groups)}."]
        if mode == "full":
            text += ["- Ở full, các nhóm 0 nút đã trả về từ tiền xử lý chiến thuật; các bàn corpus đều không trống và còn ứng viên."]
        else:
            text += ["- Ở search-only, 0 nút chỉ có thể do không có ứng viên (bàn trống/kín); cần điều tra nếu xuất hiện trong corpus này."]
    else:
        text += [f"- Không có nhóm {mode} nào có minimax_nodes=0; cả hai cờ đều vào Minimax ở mọi thế/mức."]
    text += ["- Thông tin máy/cấu hình đầy đủ: [environment.md](environment.md). "
             "Dữ liệu nguồn: [ablation-raw.csv](ablation-raw.csv).", "", "## Bất thường", ""]
    text += [f"- {item}" for item in anomalies] if anomalies else ["Không phát hiện bất thường trong các kiểm tra tự động."]
    (output / "ablation-summary.md").write_text("\n".join(text) + "\n", encoding="utf-8")
    print(f"{mode}: rows={len(rows)}, measured_pairs={comparisons}, move_differences={move_differences}, "
          f"value_differences={value_differences}, anomalies={len(anomalies)}")
    return bool(anomalies or mismatches)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-dir", type=Path, default=Path("docs/ai-results-ablation"))
    args = parser.parse_args()
    raise SystemExit(int(summarize(args.output_dir)))
