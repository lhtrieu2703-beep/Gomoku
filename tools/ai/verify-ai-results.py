"""Read-only independent replay, deduplication and summary checks for arena v2."""
import argparse,csv,hashlib,math,statistics,re
from collections import Counter,defaultdict
from pathlib import Path

def require(c,m):
    if not c:raise AssertionError(m)
def load(p):
    with p.open(encoding='utf-8',newline='') as f:return list(csv.DictReader(f))
def wins(g,r,c):
    p=g[r][c]
    if p=='.':return False
    other='O' if p=='X' else 'X'
    for dr,dc in [(1,0),(0,1),(1,1),(1,-1)]:
        n=1;blocked=0
        for sign in [-1,1]:
            y,x=r+dr*sign,c+dc*sign
            while 0<=y<15 and 0<=x<15 and g[y][x]==p:n+=1;y+=dr*sign;x+=dc*sign
            blocked+=int(0<=y<15 and 0<=x<15 and g[y][x]==other)
        if n>=5 and blocked<2:return True
    return False
def sequence(s):return [tuple(map(int,c.split(':'))) for c in s.split(';')]
def canonical(cells):
    variants=[]
    for flip in [-1,1]:
        for rot in range(4):
            stones=[]
            for i,(r,c) in enumerate(cells):
                x,y=(r-7)*flip,c-7
                for _ in range(rot):x,y=-y,x
                stones.append(('X' if i%2==0 else 'O',x,y))
            variants.append(tuple(sorted(stones)))
    return min(variants)
def table(p):return [[x.strip() for x in line.strip().strip('|').split('|')] for line in p.read_text(encoding='utf-8').splitlines() if line.startswith('| ')]
def close(a,b):
    if b is None:require(a=='NA','Expected NA');return
    require(abs(float(a)-b)<=2e-6,f'{a} != {b}')
def wilson(w,n):
    if not n:return None
    z=1.959963984540054;z2=z*z;den=2*(n+z2);root=math.sqrt(z2*(z2+4*w*(1-w/n)))
    return ((2*w+z2-root)/den,(2*w+z2+root)/den)
def binomial(w,l):
    n=w+l
    if not n:return None
    threshold=math.comb(n,w)
    return sum(math.comb(n,k) for k in range(n+1) if math.comb(n,k)<=threshold)/2**n
def check_ci(text,w,n):
    v=wilson(w,n)
    if v is None:require(text=='NA','Expected NA interval');return
    a,b=text.strip('[]').split(',');close(a,v[0]);close(b,v[1])
def counts(gs,a='A'):
    return [sum(g['result']==a+'_WIN' for g in gs),sum(g['result']=='DRAW' for g in gs),sum(g['result']==('B' if a=='A' else 'A')+'_WIN' for g in gs)]
def check_arena(out):
    games=load(out/'arena-games.csv');require(len({g['game_id'] for g in games})==len(games),'Duplicate ids')
    openings={};groups=defaultdict(list)
    for game in games:
        cells=sequence(game['sequence']);prefix=sequence(game['opening_sequence']);oid=game['opening_id'];require(2<=len(prefix)<=4,'Opening length')
        require(prefix[0]==(7,7),'Opening centre');require(all(5<=r<=9 and 5<=c<=9 for r,c in prefix),'Opening region')
        require(oid not in openings or openings[oid]==prefix,'Opening changed');openings[oid]=prefix
        require(cells[:len(prefix)]==prefix,'Wrong opening prefix');require(len(cells)==int(game['moves'])<=225,'Move limit')
        grid=[['.']*15 for _ in range(15)]
        for i,(r,c) in enumerate(cells):
            require(0<=r<15 and 0<=c<15 and grid[r][c]=='.','Illegal move');grid[r][c]='X' if i%2==0 else 'O'
            won=wins(grid,r,c);require(not won or i==len(cells)-1,'Played after win')
            if i==len(cells)-1:
                actor=game['first_actor'] if i%2==0 else ('B' if game['first_actor']=='A' else 'A')
                require(game['result']==(actor+'_WIN' if won else 'DRAW' if len(cells)==225 else 'NONTERMINAL'),'Wrong RULE_B result')
        require(not game['anomaly'],'Abnormal game');require(game['sequence_sha256']==hashlib.sha256(game['sequence'].encode()).hexdigest(),'Hash')
        require(int(game['a_moves'])+int(game['b_moves'])==len(cells)-len(prefix),'Search count includes forced moves')
        for a in ['a','b']:require(0<=float(game[a+'_mean_ms'])<=float(game[a+'_max_ms'])+1e-6,'Timing aggregate')
        groups[game['sequence']].append(game)
    require(len({canonical(v) for v in openings.values()})==len(openings),'Symmetry-equivalent openings')
    for group in groups.values():
        def key(g):return g['pair'],g['opening_id'],int(g['first_actor']!=('A' if int(g['opening_id'][1:])%2 else 'B')),g['game_id']
        representative=min(group,key=key)
        for g in group:
            require(g['included']==('1' if g is representative else '0'),'Wrong representative')
            require(g['duplicate_of']==('' if g is representative else representative['game_id']),'Wrong duplicate reference')
    pairs={'EASY-MEDIUM','EASY-HARD','MEDIUM-HARD','MEDIUM-MEDIUM','HARD-HARD'}
    require({g['pair'] for g in games}==pairs,'Wrong pairs')
    for pair in pairs:
        for oid in openings:require(Counter(g['first_actor'] for g in games if g['pair']==pair and g['opening_id']==oid)=={'A':1,'B':1},'Unpaired opening')
    rows={r[0]:r for r in table(out/'arena-summary.md') if r[0] in pairs and len(r)==11}
    sides={r[0]:r for r in table(out/'arena-summary.md') if r[0] in pairs and len(r)==5}
    included=[g for g in games if g['included']=='1']
    for pair in pairs:
        gs=[g for g in included if g['pair']==pair];w,d,l=counts(gs);n=w+l;r=rows[pair]
        require(int(r[1])==sum(g['pair']==pair for g in games) and int(r[2])==len(gs),'Summary n')
        require(r[3]==f'{w}/{d}/{l}' and r[4]==f'{l}/{d}/{w}','WDL');close(r[5],w/len(gs) if gs else None);close(r[6],w/n if n else None);check_ci(r[7],w,n);close(r[8],l/n if n else None);check_ci(r[9],l,n);require(math.isclose(float(r[10]),binomial(w,l),rel_tol=1e-7,abs_tol=1e-15) if n else r[10]=='NA','Exact binomial p')
        for index,(actor,first) in enumerate([('A',True),('A',False),('B',True),('B',False)],1):
            sub=[g for g in gs if (g['first_actor']==actor)==first];sw,sd,sl=counts(sub,actor);wdl,rate=sides[pair][index].split('; ')
            require(wdl==f'{sw}/{sd}/{sl}','Side WDL');close(rate,sw/len(sub) if sub else None)
        iv=wilson(w,n)
        if iv is None or iv[0]<=.5<=iv[1]:require(any(pair in line and 'chưa đủ bằng chứng' in line for line in (out/'arena-summary.md').read_text(encoding='utf-8').splitlines()),'Missing cautious conclusion')
    print(f'ARENA VERIFIED: openings={len(openings)}, attempted={len(games)}, unique_included={len(included)}, unique_global={len({g["sequence"] for g in games})}; legal replay, paired sides, hashes/dedup, WDL, Wilson, exact binomial, side tables')
def check_benchmark(out):
    raw=load(out/'benchmark.csv');groups=defaultdict(list)
    protocol=re.search(r'Warmups per position/level: (\d+); measured: (\d+)',(out/'environment.md').read_text(encoding='utf-8'));require(protocol is not None,'Missing benchmark protocol');warmups,repetitions=map(int,protocol.groups())
    for s in raw:groups[(s['position'],s['level'])].append(s)
    require(len(groups)==30,'Expected 10 positions x 3 levels')
    positions={}
    for pid,stones,turn,board in re.findall(r'## ([\w-]+); stones=(\d+); turn=([XO])\n.*?```text\n(.*?)```',(out/'benchmark-positions.md').read_text(encoding='utf-8'),re.S):
        grid=[list(line[4:]) for line in board.splitlines()[1:]];require(len(grid)==15 and all(len(r)==15 for r in grid),'Board dimensions')
        require(sum(c!='.' for r in grid for c in r)==int(stones),'Stone count');require(not any(wins(grid,r,c) for r in range(15) for c in range(15)),'Already won');positions[pid]=grid
    rows={(r[0],r[1]):r for r in table(out/'benchmark-summary.md') if len(r)==7 and r[0] in positions}
    for (pid,level),g in groups.items():
        require(Counter(s['phase'] for s in g)=={'warmup':warmups,'measured':repetitions},'Wrong repetition count')
        require(sorted(int(s['repetition']) for s in g if s['phase']=='measured')==list(range(1,repetitions+1)),'Repetition indices')
        for s in g:
            r,c=int(s['row']),int(s['col']);require(0<=r<15 and 0<=c<15 and positions[pid][r][c]=='.','Illegal benchmark move')
            require(int(s['depth'])=={'EASY':1,'MEDIUM':2,'HARD':3}[level],'Depth metadata')
        values=[float(s['elapsed_ms']) for s in g if s['phase']=='measured'];require(all(math.isfinite(v) and v>=0 for v in values),'Timing');r=rows[(pid,level)]
        require(int(r[2])==int(g[0]['depth']) and int(r[3])==repetitions,'Benchmark n/depth')
        for a,b in zip(r[4:],(statistics.median(values),sorted(values)[math.ceil(.95*repetitions)-1],max(values))):close(a,b)
    pooled={r[0]:r for r in table(out/'benchmark-summary.md') if len(r)==6 and r[0] in ['EASY','MEDIUM','HARD']}
    for level,r in pooled.items():
        v=[float(s['elapsed_ms']) for s in raw if s['phase']=='measured' and s['level']==level];require(int(r[1])==len(v),'Pooled n')
        for a,b in zip(r[2:],(statistics.median(v),sorted(v)[math.ceil(.95*len(v))-1],max(v),statistics.pstdev(v))):close(a,b)
    require(len(pooled)==3,'Pooled rows');print(f'BENCHMARK VERIFIED: 10 boards, {30*warmups} discarded warmups + {30*repetitions} measured; 30 per-position median/p95/max rows; pooled warning/statistics')
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--output',type=Path,default=Path(__file__).resolve().parents[2]/'docs'/'ai-results');out=p.parse_args().output;check_arena(out);check_benchmark(out)
