"""Independently decode the generated NBT and verify residents and registry links."""
from pathlib import Path
import gzip,io,json,math,struct
ROOT=Path(__file__).resolve().parents[2];D=ROOT/'src/main/resources/data/djinns'
stream=io.BytesIO(gzip.decompress((D/'structure/djin_city_capital.nbt').read_bytes()))
def unpack(fmt):return struct.unpack('>'+fmt,stream.read(struct.calcsize('>'+fmt)))[0]
def text():return stream.read(unpack('H')).decode()
def read(tag):
    if tag in [1,3,4,5,6]:return unpack({1:'b',3:'i',4:'q',5:'f',6:'d'}[tag])
    if tag==8:return text()
    if tag==9:
        kind=unpack('B');size=unpack('i');return [read(kind) for _ in range(size)]
    if tag==10:
        out={}
        while True:
            kind=unpack('B')
            if kind==0:return out
            key=text();assert key not in out;out[key]=read(kind)
    raise AssertionError('Unsupported tag '+str(tag))
assert unpack('B')==10;text();nbt=read(10);assert not stream.read()
assert nbt['DataVersion']==3955
assert nbt['size']==[97,34,97]
palette=nbt['palette'];blocks={}
for b in nbt['blocks']:
    p=tuple(b['pos']);assert p not in blocks
    assert all(0<=x<size for x,size in zip(p,nbt['size']))
    assert 0<=b['state']<len(palette)
    blocks[p]=palette[b['state']]['Name']
    if 'nbt' in b:
        assert b['nbt']['id']=='minecraft:chest'
        assert (D/('loot_table/'+b['nbt']['LootTable'].split(':')[1]+'.json')).is_file()
residents=nbt['entities'];assert len(residents)==29
ids=[e['nbt']['id'] for e in residents]
assert ids.count('djinns:djinn_king')==1
expected={f'djinns:{c}_{r}_djinn' for c in ['blue','red','white','black'] for r in ['common','merchant','guard','mage']}
assert expected<=set(ids)
for e in residents:
    x,y,z=e['pos'];assert e['nbt']['PersistenceRequired']==1
    assert e['nbt']['NoGravity']==0, 'City residents must use ground navigation'
    king=e['nbt']['id']=='djinns:djinn_king';half=.7 if king else .35;height=5 if king else 2.25
    for bx in range(math.floor(x-half),math.ceil(x+half)):
        for by in range(math.floor(y),math.ceil(y+height)):
            for bz in range(math.floor(z-half),math.ceil(z+half)):
                name=blocks.get((bx,by,bz),'minecraft:air')
                assert name in ['minecraft:air','minecraft:red_carpet'],(e['nbt']['id'],(bx,by,bz),name)
city=json.loads((D/'worldgen/structure/djin_city_capital.json').read_text())
pool=json.loads((D/'worldgen/template_pool/djin_city_capital/start.json').read_text())
structure_set=json.loads((D/'worldgen/structure_set/djin_city_capital.json').read_text())
assert city['start_pool']=='djinns:djin_city_capital/start' and city['size']==0
assert pool['elements'][0]['element']['location']=='djinns:djin_city_capital'
assert structure_set['structures'][0]['structure']=='djinns:djin_city_capital'
biomes=json.loads((D/'tags/worldgen/biome/has_structure/djin_city_capital.json').read_text())
assert biomes['values']==['minecraft:desert']
print(f'PASS: NBT decoded independently; {len(blocks)} unique blocks, valid palette/positions, 29 unobstructed residents, all 16 variants and one king; desert worldgen links and chest loot valid.')
