"""Exercise actual item display transforms through vanilla held-item matrices."""
from pathlib import Path
import json,math,zipfile,sys
ROOT=Path(__file__).resolve().parents[2]
with zipfile.ZipFile(sys.argv[1]) as jar:
 model=json.loads(jar.read('assets/djinns/models/item/royal_sword.json'))
 metadata=json.loads(jar.read('fabric.mod.json'));assert metadata['version']=='1.1.1'
 assert not any(n.startswith(('net/minecraft/','net/fabricmc/','com/mojang/brigadier/')) for n in jar.namelist()),'API test fixtures leaked into distribution'
def rotate(v,axis,degrees):
 a,b=(axis+1)%3,(axis+2)%3;out=list(v);c=math.cos(math.radians(degrees));s=math.sin(math.radians(degrees))
 out[a]=v[a]*c-v[b]*s;out[b]=v[a]*s+v[b]*c;return out
def transform(point,hand):
 display=model['display']['thirdperson_'+hand+'hand'];mirror=-1 if hand=='left' else 1
 v=[(p-8)*s for p,s in zip(point,display['scale'])]
 # Vanilla item transforms multiply rotations X, Y, Z, then scale.
 for axis in [2,1,0]:v=rotate(v,axis,display['rotation'][axis]*(-1 if mirror<0 and axis in (1,2) else 1))
 t=display['translation'];v=[v[0]+t[0]*mirror+mirror,v[1]+t[1]+2,v[2]+t[2]-10]
 v=rotate(v,1,180);return rotate(v,0,-90)
for hand,x in [('right',-2),('left',2)]:
 grip=transform([8,-3.35,8],hand)
 assert max(abs(a-b) for a,b in zip(grip,[x,12,0]))<.01,(hand,grip)
 tip=transform([8,31.7,8],hand)
 assert tip[1]<grip[1]-29 and abs(tip[0]-grip[0])<.001 and abs(tip[2]-grip[2])<.001,(hand,tip)
print('PASS: actual third-person transforms place the handle at both palms (error < 0.01 model pixel), with an upright blade; no API fixtures in JAR.')
