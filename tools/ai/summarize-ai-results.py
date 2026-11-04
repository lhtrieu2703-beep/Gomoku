import argparse,csv,hashlib,math,statistics
from collections import defaultdict
from pathlib import Path

def load(p):
    with p.open(encoding='utf-8',newline='') as f:return list(csv.DictReader(f))
def interval(w,n):
    if not n:return None
    z=1.959963984540054;q=w/n;d=1+z*z/n
    c=(q+z*z/(2*n))/d;h=z*math.sqrt(q*(1-q)/n+z*z/(4*n*n))/d
    return c-h,c+h
def pvalue(w,l):
    n=w+l
    return min(1,2*sum(math.comb(n,i) for i in range(min(w,l)+1))/2**n) if n else None
def fmt(x):return 'NA' if x is None else f'{x:.6f}'
def ci(w,n):
    v=interval(w,n);return 'NA' if v is None else f'[{v[0]:.6f}, {v[1]:.6f}]'
def counts(gs,actor='A'):
    return [sum(g['result']==actor+'_WIN' for g in gs),sum(g['result']=='DRAW' for g in gs),sum(g['result']==('B' if actor=='A' else 'A')+'_WIN' for g in gs)]
def deduplicate(games):
    groups=defaultdict(list)
    for g in games:
        g['sequence_sha256']=hashlib.sha256(g['sequence'].encode()).hexdigest();groups[g['sequence']].append(g)
    for group in groups.values():
        def priority(g):
            preferred='A' if int(g['opening_id'][1:])%2 else 'B'
            return g['pair'],g['opening_id'],int(g['first_actor']!=preferred),g['game_id']
        keep=min(group,key=priority)
        for g in group:g['included']='1' if g is keep else '0';g['duplicate_of']='' if g is keep else keep['game_id']
    return sorted(games,key=lambda g:g['game_id'])
def summarize(out):
    games=deduplicate(load(out/'arena-games.csv'))
    with (out/'arena-games.csv').open('w',encoding='utf-8',newline='') as f:
        w=csv.DictWriter(f,fieldnames=list(games[0]),lineterminator='\n');w.writeheader();w.writerows(games)
    included=[g for g in games if g['included']=='1'];global_unique=len({g['sequence'] for g in games})
    openings={g['opening_id'] for g in games}
    text=['# Arena v2','',f'Openings: {len(openings)}; attempted games: {len(games)}; globally unique / included: {len(included)}; exact sequences unique globally: {global_unique}; excluded duplicate rows: {len(games)-len(included)}.','',
    'RULE_B, 15x15, X black starts; win before full-board draw. Each fixed opening is forced for both color assignments; engine play starts afterwards. Opening time is excluded from per-actor search timings. Each game stops at a win or 225 placements.',
    '', 'Deduplicate exact full color-coordinate sequences GLOBALLY, including across pairings; only one row per sequence contributes to statistics. All paired attempts remain available for audit. No symmetry folding of completed games. All attempted rows remain in CSV with sequence, hash, included and duplicate_of. Representative choice is deterministic: lexicographically first pair then opening id, preferring A-first on odd opening ids / B-first on even ids (not selected by outcome). Deduplication can unbalance color counts; the side table uses actual retained denominators.',
    '', 'Draws are reported but excluded from Wilson 95% intervals and the exact two-sided binomial test H0 p=0.5. Win/all uses all retained games; decisive rate uses wins/(wins+losses). B has complementary decisive rate/interval and the same p-value. NA means no decisive games.',
    '', '**Inference limits:** Wilson/binomial assume independent Bernoulli trials. Paired openings and deterministic continuations violate strict independence; intervals/p-values here are nominal descriptive diagnostics, not confirmatory evidence. Five comparisons are unadjusted. Self-play actor A/B comparisons have no engine-strength meaning. Fixed small synthetic corpus, uncontrolled EASY RNG, and dedup selection preclude Elo/general superiority claims.',
    '', '| Pair A-B | Attempts | Unique | A W/D/L | B W/D/L | A win/all | A decisive | A Wilson95 | B decisive | B Wilson95 | p two-sided |', '|---|---:|---:|---|---|---:|---:|---|---:|---|---:|']
    pairs=list(dict.fromkeys(g['pair'] for g in games))
    for pair in pairs:
        gs=[g for g in included if g['pair']==pair];w,d,l=counts(gs);n=w+l
        text.append(f'| {pair} | {sum(g["pair"]==pair for g in games)} | {len(gs)} | {w}/{d}/{l} | {l}/{d}/{w} | {fmt(w/len(gs) if gs else None)} | {fmt(w/n if n else None)} | {ci(w,n)} | {fmt(l/n if n else None)} | {ci(l,n)} | {format(pvalue(w,l),".8g") if n else "NA"} |')
    text+=['','## Side effect (retained rows only)','', 'Each entry is W/D/L; win/all. A/B identify engines named by the pair, including distinct seats in self-play.','', '| Pair | A as X / first | A as O / second | B as X / first | B as O / second |','|---|---|---|---|---|']
    for pair in pairs:
        gs=[g for g in included if g['pair']==pair];cells=[]
        for actor,first in [('A',True),('A',False),('B',True),('B',False)]:
            sel=[g for g in gs if (g['first_actor']==actor)==first];w,d,l=counts(sel,actor)
            cells.append(f'{w}/{d}/{l}; {fmt(w/len(sel) if sel else None)}')
        text.append('| '+pair+' | '+' | '.join(cells)+' |')
    text+=['','## Conclusions','']
    for pair in pairs:
        gs=[g for g in included if g['pair']==pair];w,d,l=counts(gs);iv=interval(w,w+l)
        conclusion='chưa đủ bằng chứng (no decisive games)' if iv is None else 'chưa đủ bằng chứng: Wilson95 contains 0.5' if iv[0]<=.5<=iv[1] else 'Observed imbalance within this protocol; nominal interval excludes 0.5, but paired/dependent data do not establish general engine superiority'
        text.append(f'- {pair}: {conclusion}.')
    text+=['','Full exact sequences replace arena-moves.csv; EASY engine randomness remains uncontrolled, while opening seed 20261031 and all recorded positions are reproducible.']
    (out/'arena-summary.md').write_text('\n'.join(text)+'\n',encoding='utf-8',newline='\n')
    raw=load(out/'benchmark.csv');measured=[s for s in raw if s['phase']=='measured'];groups=defaultdict(list)
    for s in measured:groups[(s['position'],s['level'],s['depth'])].append(float(s['elapsed_ms']))
    def metrics(v):return statistics.median(v),sorted(v)[math.ceil(.95*len(v))-1],max(v)
    lines=['# Benchmark v2','', 'Sequential, direct MinimaxAI calls. Warmups discarded. Wall time excludes board copy/invariant checks. p95 uses nearest-rank ceil(0.95*n). Depth 1 is EASY enum configuration; EASY does not execute recursive Minimax. Zero nodes may mean a tactical early return.', '', '| Position | Level | Depth | n | median ms | p95 ms | max ms |','|---|---|---:|---:|---:|---:|---:|']
    for (pos,level,depth),v in groups.items():lines.append(f'| {pos} | {level} | {depth} | {len(v)} | '+ ' | '.join(fmt(x) for x in metrics(v))+' |')
    lines+=['','## Pooled by level (descriptive only)','', '**Warning: large between-position dispersion and tactical early returns. Pooled medians MUST NOT be used to conclude speed superiority, responsiveness on arbitrary boards, or playing strength. Inspect the per-position table.**', '', '| Level | n | median ms | p95 ms | max ms | SD ms |','|---|---:|---:|---:|---:|---:|']
    for level in ['EASY','MEDIUM','HARD']:
        v=[float(s['elapsed_ms']) for s in measured if s['level']==level]
        lines.append(f'| {level} | {len(v)} | '+' | '.join(fmt(x) for x in (*metrics(v),statistics.pstdev(v)))+' |')
    lines+=['',f'Measured rows: {len(measured)}; warmups: {len(raw)-len(measured)}. Construction/ASCII in benchmark-positions.md; no positions selected based on AI output. Late 40/60/100 are synthetic density categories, not representative human endgames. Small timing sample; JIT/GC/OS scheduling remain factors.']
    (out/'benchmark-summary.md').write_text('\n'.join(lines)+'\n',encoding='utf-8',newline='\n')
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--output',type=Path,default=Path(__file__).resolve().parents[2]/'docs'/'ai-results');summarize(p.parse_args().output)
