"""Render the exported assets for inspection; these are not Minecraft screenshots."""
from pathlib import Path
import json,math
import numpy as np
from PIL import Image,ImageDraw
ROOT=Path(__file__).resolve().parents[2];A=ROOT/'src/main/resources/assets/djinns';OUT=ROOT/'build/previews'

def rotation(x=0,y=0,z=0):
    sx,cx=math.sin(x),math.cos(x);sy,cy=math.sin(y),math.cos(y);sz,cz=math.sin(z),math.cos(z)
    return np.array([[cz,-sz,0],[sz,cz,0],[0,0,1]])@np.array([[cy,0,sy],[0,1,0],[-sy,0,cy]])@np.array([[1,0,0],[0,cx,-sx],[0,sx,cx]])

def cube_faces(box,matrix,offset,color):
    x,y,z,w,h,d=box
    v=np.array([[x,y,z],[x+w,y,z],[x+w,y+h,z],[x,y+h,z],[x,y,z+d],[x+w,y,z+d],[x+w,y+h,z+d],[x,y+h,z+d]])
    v=(matrix@v.T).T+offset
    return [(v[list(ids)],color) for ids in [(0,1,2,3),(4,7,6,5),(0,4,5,1),(3,2,6,7),(0,3,7,4),(1,5,6,2)]]

def render(faces,path,flip_y=False,tilt=0):
    camera=rotation(math.radians(-12),math.radians(24),math.radians(tilt))
    if flip_y:camera=camera@np.diag([1,-1,1])
    projected=[]
    light=np.array([-.4,.8,-.7]);light/=np.linalg.norm(light)
    for vertices,color in faces:
        v=(camera@vertices.T).T
        normal=np.cross(v[1]-v[0],v[2]-v[0]);length=np.linalg.norm(normal)
        if length<.00001:continue
        shade=.66+.34*abs(float(np.dot(normal/length,light)))
        projected.append((v,color,shade))
    allv=np.concatenate([v for v,_,_ in projected]);lo=allv[:,:2].min(0);hi=allv[:,:2].max(0)
    scale=min(540/(hi[0]-lo[0]),620/(hi[1]-lo[1]));center=(hi+lo)/2
    im=Image.new('RGB',(640,740),(27,31,37));draw=ImageDraw.Draw(im)
    for v,color,shade in sorted(projected,key=lambda f:f[0][:,2].mean(),reverse=True):
        points=[(320+(p[0]-center[0])*scale,360-(p[1]-center[1])*scale) for p in v]
        draw.polygon(points,fill=tuple(int(c*shade) for c in color))
    draw.text((18,712),'PREVIA DO MODELO EXPORTADO - FORA DO MINECRAFT',fill=(189,195,199))
    im.save(path)

texture=Image.open(A/'textures/entity/king.png').convert('RGB');faces=[]
def walk(node,matrix=np.eye(3),offset=np.zeros(3)):
    if not node.get('field_3665',True):return
    pivot=np.array([node.get('field_3657',0),node.get('field_3656',0),node.get('field_3655',0)])
    offset=offset+matrix@pivot
    matrix=matrix@rotation(node.get('field_3654',0),node.get('field_3675',0),node.get('field_3674',0))
    for box,uv in zip(node.get('boxes',[]),node.get('uvs',[])):
        color=texture.getpixel((min(127,uv[0]+2),min(127,uv[1]+2)))
        faces.extend(cube_faces(box,matrix,offset,color))
    for child in node.get('children',{}).values():walk(child,matrix,offset)
walk(json.loads((OUT/'king-geometry.json').read_text()))
render(faces,OUT/'king-model.png',True)
model=json.loads((A/'models/item/royal_sword.json').read_text());atlas=Image.open(A/'textures/item/royal_blade_atlas.png').convert('RGB');faces=[]
for e in model['elements']:
    uv=e['faces']['north']['uv'];color=atlas.getpixel((int(uv[0]*2)+1,int(uv[1]*2)+1))
    box=e['from']+[b-a for a,b in zip(e['from'],e['to'])]
    faces.extend(cube_faces(box,np.eye(3),np.zeros(3),color))
render(faces,OUT/'riftblade-model.png',False,42)
print('Rendered exported king and Riftblade voxel geometry for inspection.')
