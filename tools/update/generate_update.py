"""Original 1.1.0 assets: voxel Riftblade, royal geometry and a desert capital."""
from pathlib import Path
import gzip
import json
import math
import random
import struct
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
RES = ROOT / 'src/main/resources'
A = RES / 'assets/djinns'
D = RES / 'data/djinns'

def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + '\n')

def geometry():
    parts = []
    def part(parent, name, pivot, boxes, royal=True):
        parts.append(dict(parent=parent, name=name, pivot=pivot, royal=royal, cubes=boxes))
    def box(uv, *coords): return dict(uv=uv, box=list(coords))
    gold=[64,32]; linen=[32,32]; gem=[32,96]; skin=[0,0]; mist=[64,0]
    crown = [box(gold,-5.7,-10.9,-5.7,11.4,1.2,11.4),box(gold,-4.9,-12.0,-4.9,9.8,1.2,9.8)]
    for x in [-4,-2,0,2,4]:
        crown.append(box(gold,x-.55,-13.3,-5.8,1.1,2.5,1))
    crown += [box(gem,-1.4,-13.7,-6.1,2.8,4.3,.8),box(gold,-1.9,-11.8,-5.9,3.8,1,1)]
    crown += [box(gold,-3.4,-4.4,-4.4,6.8,.55,.65),box(skin,-.55,-2.8,-4.55,1.1,2.3,.6),
              box(gold,-3.6,1.0,-4.1,7.2,.55,.65)]
    for x in [-4.6,3.1]:
        crown += [box(linen,x,-6,3.4,1.5,14,1.2),box(gold,x-.1,7,3.3,1.7,1.5,1.4)]
    part('head','royal_crown',[0,0,0],crown)
    cape = [box(linen,-5.5,0,0,11,14,1),box(gold,-5.7,12,-.1,11.4,1.5,1.2)]
    for x in [-4.2,0,4.2]: cape.append(box(gold,x-.4,1,-.15,.8,11,1.4))
    part('spirit','royal_cape',[0,-22,3.6],cape)
    armor = [box(gold,-5.7,-23,-3.8,11.4,2,1.5),box(gem,-1.4,-23.2,-4.1,2.8,3.8,1),
             box(gold,-5.7,-18.5,-3.8,11.4,1.2,1.5),box(gold,-3.8,-20.8,-3.85,1,2.3,1.2),
             box(gold,2.8,-20.8,-3.85,1,2.3,1.2)]
    for x,y in [(-4.5,-21.5),(-3.2,-20.5),(-1.8,-19.9),(1.8,-19.9),(3.2,-20.5),(4.5,-21.5)]:
        armor += [box(gold,x-.45,y,-4.25,.9,.9,.9),box(linen,x-.22,y+.15,-4.4,.44,.44,.2)]
    part('spirit','royal_chain',[0,0,0],armor)
    for arm, sign in [('right_arm',-1),('left_arm',1)]:
        x=-4 if sign<0 else 0
        part(arm,'forearm',[0,6,0],[box(skin,x,0,-2,4,6,4),box(gold,x-.35,3,-2.35,4.7,2,4.7),
              box(skin,x,5,-2.1,4,2,4.2)],False)
        outer=-5 if sign<0 else -.5
        shoulder=[box(gold,outer,-2.6,-3.3,5.5,2.4,6.6),
                  box(gold,outer-.4,-1,-3.5,6.3,1.5,7),
                  box(gem,(-3.4 if sign<0 else 1.8),-.5,-3.8,1.6,2.2,.8)]
        for level in range(3):shoulder.append(box(gold,outer+(.1 if sign<0 else 3.9),.4+level*.8,-2.9,1.5,.65,5.8))
        part(arm,'royal_pauldron',[0,0,0],shoulder)
        part(arm+'/forearm','royal_gauntlet',[0,0,0],[box(gold,x-.5,.5,-2.6,5,4.3,5.2),
              box(gem,x+1.3,1.4,-2.9,1.4,2.6,.7)])
    part('spirit','royal_tabard',[0,-13,-3.7],[box(linen,-3,0,0,6,8,1),
          box(gold,-3.1,6.8,-.1,6.2,1.2,1.2),box(gold,-.45,.2,-.2,.9,6.7,1.4)])
    part('tail','royal_mist',[0,0,0],[box(mist,-4,0,-3,8,3,6),box(gem,-.9,1,-3.2,1.8,3,.7)])
    part('tail/tail2/tail3','tail4',[0,5,0],[box(mist,-.9,0,-.9,1.8,3,1.8)],False)
    write_json(A/'geometry/royal.json',dict(parts=parts))
    # Keep the named development source equivalent to the exported production geometry.
    def parent_expr(path):
        p=path.split('/');expr={'spirit':'spirit','head':'head','tail':'tail'}.get(p[0],f'spirit.getChild("{p[0]}")')
        return expr+''.join(f'.getChild("{n}")' for n in p[1:])
    lines=['package dev.minesla.djinn.client;','import net.minecraft.client.model.*;',
           '/** Generated from assets/djinns/geometry/royal.json. */','public final class RoyalGeometry {',
           '    private RoyalGeometry() {}','    public static void add(ModelPartData spirit, ModelPartData head, ModelPartData tail) {']
    for p in parts:
        text='        '+parent_expr(p['parent'])+'.addChild("'+p['name']+'", ModelPartBuilder.create()'
        for c in p['cubes']:text+='\n            .uv('+','.join(map(str,c['uv']))+').cuboid('+','.join(str(float(x))+'f' for x in c['box'])+')'
        text+=', ModelTransform.pivot('+','.join(str(float(x))+'f' for x in p['pivot'])+'));';lines.append(text)
    lines+=['    }','}']
    (ROOT/'src/client/java/dev/minesla/djinn/client/RoyalGeometry.java').write_text('\n'.join(lines)+'\n')
    return parts

def royal_texture():
    im = Image.open(A/'textures/entity/king.png').convert('RGBA')
    rnd=random.Random(2110); px=im.load()
    regions=[((0,0,64,32),(20,39,55)),((0,32,32,64),(16,31,43)),
             ((32,32,64,64),(212,198,165)),((64,0,96,32),(14,140,157)),
             ((64,32,96,64),(212,163,64)),((96,32,128,64),(30,75,91)),
             ((64,64,128,96),(223,209,178))]
    for (x0,y0,x1,y1),color in regions:
        for y in range(y0,y1):
            for x in range(x0,x1):
                n=rnd.randint(-8,8)+(8 if (y%5==0) else 0)
                px[x,y]=tuple(max(0,min(255,c+n)) for c in color)+(255,)
    draw=ImageDraw.Draw(im)
    for y in [36,40,45,50,56,61]: draw.line((32,y,63,y),fill=(163,147,119))
    for x in range(65,95,7):
        draw.line((x,32,x,63),fill=(249,216,130));draw.line((x+2,32,x+2,63),fill=(151,106,39))
    for x,y in [(5,7),(13,11),(23,5),(37,15),(49,8),(10,22)]:
        draw.line((x,y,x+3,y+3),fill=(22,130,149));draw.point((x+3,y+3),fill=(31,215,216))
    draw.rectangle((0,96,8,101),fill=(49,255,239));draw.rectangle((32,96,40,106),fill=(17,201,216))
    draw.line((33,97,33,103),fill=(196,255,244));draw.line((34,97,37,97),fill=(127,255,235))
    im.save(A/'textures/entity/king.png')

def sword():
    colors={'obsidian':(13,25,34),'steel':(24,59,70),'edge':(39,148,161),'cyan':(18,202,218),
            'light':(147,255,231),'teal':(10,130,133),'gold':(205,160,65),'ivory':(239,229,180),
            'grip':(19,29,39),'darkgold':(132,93,30)}
    atlas=Image.new('RGBA',(32,32*6));rnd=random.Random(740)
    tiles={name:(i%4*8,i//4*8) for i,name in enumerate(colors)}
    for frame in range(6):
        for name,c in colors.items():
            x0,y0=tiles[name]
            for y in range(8):
                for x in range(8):
                    n=rnd.randint(-7,7)
                    pulse=int(12*math.sin(frame*math.pi/3+(x+y)*.25)) if name in ['cyan','teal','light'] else 0
                    atlas.putpixel((x0+x,y0+y+frame*32),tuple(max(0,min(255,a+n+pulse)) for a in c)+(255,))
    texture=A/'textures/item/royal_blade_atlas.png';atlas.save(texture)
    write_json(texture.with_suffix('.png.mcmeta'),{'animation':{'frametime':3,'interpolate':True,'width':32,'height':32}})
    elements=[]
    def cube(x,y,z,w,h,d,material,shade=True):
        u,v=tiles[material]
        faces={face:{'uv':[u/2,v/2,(u+6)/2,(v+6)/2],'texture':'#blade'} for face in ['north','south','east','west','up','down']}
        elements.append({'from':[x,y,z],'to':[x+w,y+h,z+d],'shade':shade,'faces':faces})
    # True voxel geometry, with a dark central fuller and cyan broken flame edges.
    cube(6.5,5,7.35,3,24,.9,'obsidian')
    cube(6,6,7.5,.7,23,.65,'edge');cube(9.3,6,7.5,.7,23,.65,'edge')
    cube(7.1,6,7.22,.6,23,1.18,'steel');cube(8.5,6,7.20,.35,23,1.2,'teal')
    cube(7,29,7.4,2,1.5,.7,'edge');cube(7.6,30.5,7.5,.8,1.2,.5,'light',False)
    for side in [-1,1]:
        tongues=[(6,1.6,2.5),(9.5,3.3,1.6),(13,1.5,3.1),(17.2,2.7,2.2),(22,1.3,3),(26.5,2.1,1.8)] if side<0 else [(7.2,1.1,3.0),(12,2.3,1.8),(15.5,3.1,2.6),(20,1.2,2.2),(23.5,2.4,1.9),(28.5,1.3,1.6)]
        for i,(y,width,height) in enumerate(tongues):
            x=6-width if side<0 else 10
            cube(x,y,7.35,width,height,.8,'cyan',False)
            cube(x+(.2 if side<0 else width-.6),y+.8,7.45,.6,height*.8,.6,'teal',False)
            if i%2==0:cube(x+(width*.4),y+.1,7.2,.5,height*.65,1.1,'light',False)
            cube(x+(.1 if side<0 else width-.4),y+height-.2,7.5,.45,1.1,.5,'cyan',False)
        for x,y in [(3.1,11),(2.8,19),(4,27),(11.9,15),(12.4,24),(10.8,29.5)]:
            if (side<0)==(x<8):cube(x,y,7.6,.65,.85,.65,'cyan',False)
    for y in [8,13,18,23,27]:cube(7.7,y,7.1,.6,1.2,1.45,'edge',False)
    # Ceremonial gold/ivory guard and lapis gem, matching the supplied silhouette.
    cube(3.8,3.2,6.7,8.4,1.6,2.6,'gold');cube(5.4,2.3,6.4,5.2,2.7,3.2,'darkgold')
    cube(6.6,3,6,2.8,3.7,4,'gold');cube(7.2,4,5.7,1.6,2.3,4.6,'light',False)
    for side in [-1,1]:
        x=3.1 if side<0 else 11.9
        cube(x,3.6,6.9,1.1,2.7,2.2,'ivory');cube(x-.3,5.9,7.1,1.1,1.3,1.8,'gold')
        cube(x+(.8 if side<0 else -.8),1.8,6.9,1.1,2,2.2,'ivory')
    cube(7,-9,7,2,11.3,2,'grip')
    for y in [-8.8,-6,-3.2,-.4]:cube(6.8,y,6.8,2.4,.55,2.4,'steel')
    for y in [-9.7,1.2]:cube(6.5,y,6.5,3,1,3,'gold')
    cube(6.3,-12.6,6.3,3.4,2.6,3.4,'gold')
    cube(7,-13.4,6.8,2,1,2.4,'ivory');cube(6.7,-11.8,5.9,2.6,1.3,.5,'ivory')
    model={'credit':'Original voxel geometry inspired by the supplied Riftblade reference',
           'ambientocclusion':False,'gui_light':'front','textures':{'blade':'djinns:item/royal_blade_atlas','particle':'djinns:item/royal_blade_atlas'},
           'elements':elements,'display':{
               'thirdperson_righthand':{'rotation':[90,0,0],'translation':[1,-2,7.65],'scale':[.85,.85,.85]},
               'thirdperson_lefthand':{'rotation':[90,0,0],'translation':[1,-2,7.65],'scale':[.85,.85,.85]},
               'firstperson_righthand':{'rotation':[0,-10,-12],'translation':[.5,6,-1],'scale':[.65,.65,.65]},
               'firstperson_lefthand':{'rotation':[0,10,12],'translation':[.5,6,-1],'scale':[.65,.65,.65]},
               'gui':{'rotation':[0,0,-42],'translation':[0,0,0],'scale':[.34,.34,.34]},
               'ground':{'rotation':[0,0,0],'translation':[0,6,0],'scale':[.4,.4,.4]},
               'fixed':{'rotation':[0,180,-42],'translation':[0,0,0],'scale':[.4,.4,.4]}}}
    write_json(A/'models/item/royal_sword.json',model)
    return elements

# Minimal NBT encoder: no runtime library is added to the mod.
def nbt_payload(tag, value):
    if tag==1:return struct.pack('>b',value)
    if tag==3:return struct.pack('>i',value)
    if tag==4:return struct.pack('>q',value)
    if tag==5:return struct.pack('>f',value)
    if tag==6:return struct.pack('>d',value)
    if tag==8:
        b=value.encode();return struct.pack('>H',len(b))+b
    if tag==9:
        kind,items=value;return bytes([kind])+struct.pack('>i',len(items))+b''.join(nbt_payload(kind,v) for v in items)
    if tag==10:return b''.join(bytes([t])+nbt_payload(8,k)+nbt_payload(t,v) for k,(t,v) in value.items())+b'\0'
    raise ValueError(tag)

def city():
    blocks={}; block_entities={}; residents=[]
    def setb(x,y,z,name,props=None,nbt=None):
        if not(0<=x<97 and 0<=z<97 and 0<=y<58):return
        key=(x,y,z);blocks[key]=(name,tuple(sorted((props or {}).items())))
        if nbt:block_entities[key]=nbt
        else:block_entities.pop(key,None)
    def fill(x0,y0,z0,x1,y1,z1,name,props=None):
        for x in range(x0,x1+1):
            for y in range(y0,y1+1):
                for z in range(z0,z1+1):setb(x,y,z,name,props)
    def dome(cx,base,cz,r):
        for dx in range(-r,r+1):
            for dz in range(-r,r+1):
                for dy in range(0,r+1):
                    dist=math.sqrt(dx*dx+dy*dy+dz*dz)
                    if r-.9<=dist<=r+.1:setb(cx+dx,base+dy,cz+dz,'warped_planks' if dy%3 else 'dark_prismarine')
        for h,name in enumerate(['gold_block','lightning_rod','lightning_rod']):setb(cx,base+r+1+h,cz,name,{'facing':'up','waterlogged':'false'} if name=='lightning_rod' else None)
    def lamp(x,y,z):
        setb(x,y,z,'chiseled_sandstone');setb(x,y+1,z,'lantern',{'hanging':'false','waterlogged':'false'})
    def arch(x0,y,z0,width,height,axis='x'):
        for i in range(width):
            for h in range(height):
                if i in [0,width-1] or h==height-1 or h>=height-3 and i in [1,width-2]:
                    setb(x0+i if axis=='x' else x0,y+h,z0 if axis=='x' else z0+i,'cut_sandstone')
    def room(x,z,w,d,height=7):
        fill(x,3,z,x+w,3+height,z+d,'smooth_sandstone')
        fill(x+1,4,z+1,x+w-1,2+height,z+d-1,'air')
        fill(x,3+height,z,x+w,3+height,z+d,'cut_sandstone')
        fill(x+1,3,z,x+w-1,3,z+d-1,'smooth_sandstone')
        # Open arch doorway; djinns are tall and need at least three blocks.
        center=x+w//2;fill(center-1,4,z,center+1,7,z,'air');arch(center-2,4,z,5,5)
        for side in [x,x+w]:
            for zz in [z+2,z+d-2]:
                fill(side,6,zz,side,7,zz,'cyan_stained_glass')
        for xx in range(x+1,x+w,3):setb(xx,4+height,z,'sandstone_wall',{'up':'true','north':'none','south':'none','east':'none','west':'none'})
        for px,pz in [(x+1,z+1),(x+w-1,z+d-1)]:lamp(px,4,pz)
        setb(x+2,4,z+d-2,'red_carpet')
    def djinn(x,y,z,color,role,job=0,king=False):
        name='djinn_king' if king else f'{color}_{role}_djinn'
        data={'id':(8,'djinns:'+name),'PersistenceRequired':(1,1),'NoGravity':(1,0),'DjinnJob':(3,job),
              'Rotation':(9,(5,[180.0,0.0])),
              'Health':(5,300.0 if king else 48.0 if role=='guard' else 36.0 if role=='mage' else 28.0)}
        if king or role in ['guard','mage']:
            weapon='djinns:royal_sword' if king else 'minecraft:blaze_rod' if role=='mage' else 'djinns:desert_spear'
            hands=[{'id':(8,weapon),'count':(3,1)}, {'id':(8,'djinns:desert_shield'),'count':(3,1)} if role=='guard' and not king else {}]
            data['HandItems']=(9,(10,hands));data['HandDropChances']=(9,(5,[1.0 if king else .12, .08]))
        residents.append({'pos':(9,(6,[float(x)+.5,float(y),float(z)+.5])),
                          'blockPos':(9,(3,[x,y,z])),'nbt':(10,data)})

    # Raised, stepped foundation and a ring wall, not a solid mountain of blocks.
    fill(1,0,1,95,0,95,'sandstone');fill(2,1,2,94,2,94,'cut_sandstone');fill(3,3,3,93,3,93,'smooth_sandstone')
    # Broad central avenue and arcaded cross street, with contrasting paving.
    fill(44,3,3,52,3,92,'chiseled_sandstone');fill(5,3,41,91,3,49,'cut_sandstone')
    for side in [3,93]:
        fill(side,4,3,side,8,93,'sandstone');fill(3,4,side,93,8,side,'sandstone')
    for i in range(4,94,3):
        for x,z in [(i,3),(i,93),(3,i),(93,i)]:setb(x,9,z,'chiseled_sandstone')
    fill(44,4,3,52,11,3,'air');arch(42,4,3,13,12)
    for z in range(4):fill(44,z,z,52,z,z,'sandstone_stairs',{'facing':'south','half':'bottom','shape':'straight','waterlogged':'false'})
    for x in [42,54]:fill(x,4,2,x,14,4,'cut_sandstone')
    # Two shallow canals with low edges and lit bridges.
    for x in [39,55]:
        fill(x,2,11,x+2,2,79,'prismarine_bricks');fill(x,3,11,x+2,3,79,'water',{'level':'0'})
        for side in [x-1,x+3]:fill(side,3,10,side,3,80,'cut_sandstone')
        for z in [21,45,67]:fill(x-1,4,z-1,x+3,4,z+1,'smooth_sandstone');lamp(x-1,5,z);lamp(x+3,5,z)
    # Homes around the outer districts, with bed/workstation interiors.
    positions=[(9,13),(23,13),(66,13),(80,13),(9,29),(24,29),(66,29),(80,29),(9,57),(24,57),(66,57),(80,57)]
    jobs=['composter','barrel','loom','fletching_table','lectern','cartography_table','brewing_stand',
          'blast_furnace','grindstone','smithing_table','smoker','cauldron','stonecutter']
    for i,(x,z) in enumerate(positions):
        room(x,z,8,9,7)
        if i%3==0:dome(x+4,11,z+5,4)
        else:
            for xx in [x+1,x+7]:fill(xx,11,z+1,xx,12,z+8,'sandstone')
        setb(x+2,4,z+5,jobs[i%len(jobs)])
        setb(x+6,4,z+7,'chest',{'facing':'south','type':'single','waterlogged':'false'},
             {'id':(8,'minecraft:chest'),'LootTable':(8,'djinns:chests/capital')})
        djinn(x+4,4,z+5,['blue','red','white','black'][i%4],'common',i%13)
    # Bazaar: dyed cloth canopies, decorative stock, lapis merchants.
    for i,x in enumerate([10,21,66,77]):
        z=45
        fill(x,4,z+8,x+7,4,z+8,'smooth_sandstone')
        for px in [x,x+7]:fill(px,4,z+3,px,7,z+8,'oak_fence')
        for xx in range(8):
            fill(x+xx,8,z+2,x+xx,8,z+9,'cyan_wool' if (xx+i)%2==0 else 'white_wool')
        setb(x+2,5,z+8,'lapis_block');setb(x+5,5,z+8,'flower_pot')
        djinn(x+4,4,z+5,['blue','red','white','black'][i],'merchant',i+7)
    # Central fountain.
    for dx in range(-6,7):
        for dz in range(-6,7):
            d=dx*dx+dz*dz
            if 20<=d<=36:setb(48+dx,4,45+dz,'smooth_sandstone')
            elif d<20:setb(48+dx,4,45+dz,'water',{'level':'0'})
    fill(48,4,45,48,7,45,'chiseled_sandstone');setb(48,8,45,'sea_lantern')
    # Palace: a high throne hall with galleries and a monumental turquoise dome.
    room(30,72,36,20,15)
    fill(43,4,72,53,13,72,'air');arch(41,4,72,15,12)
    fill(45,4,74,51,4,87,'red_carpet')
    for x in [34,62]:
        for z in [76,82,88]:
            fill(x,4,z,x+1,16,z+1,'cut_sandstone');fill(x-1,16,z-1,x+2,17,z+2,'chiseled_sandstone')
    fill(43,4,86,53,4,90,'gold_block');fill(44,5,87,52,5,90,'smooth_sandstone')
    fill(46,6,89,50,10,90,'gold_block');fill(47,6,88,49,6,89,'red_wool')
    djinn(48,7,85,'blue','guard',king=True)
    for x in [35,61]:djinn(x,4,78,'blue','guard',8)
    dome(48,19,82,10)
    # Four minarets: stair-like silhouettes, balconies and small copper-blue domes.
    for cx,cz in [(11,80),(84,80),(12,7),(84,7)]:
        fill(cx-2,3,cz-2,cx+2,23,cz+2,'cut_sandstone')
        fill(cx-1,4,cz-1,cx+1,22,cz+1,'air')
        fill(cx-3,20,cz-3,cx+3,20,cz+3,'chiseled_sandstone')
        for x,z in [(cx-2,cz),(cx+2,cz),(cx,cz-2),(cx,cz+2)]:setb(x,22,z,'sea_lantern')
        dome(cx,24,cz,3)
    # A pair of stylized djinn statues at the entrance.
    for cx in [35,61]:
        fill(cx-2,3,6,cx+2,4,10,'chiseled_sandstone')
        fill(cx,5,8,cx,7,8,'dark_prismarine');fill(cx-1,8,7,cx+1,11,9,'polished_deepslate')
        fill(cx-3,10,8,cx+3,10,8,'gold_block');fill(cx-1,12,7,cx+1,14,9,'polished_deepslate')
        fill(cx-2,15,6,cx+2,15,10,'cut_sandstone');setb(cx-1,13,6,'sea_lantern');setb(cx+1,13,6,'sea_lantern')
    # Date-palm shapes and pots, with warm lamps lining the approach.
    for x,z in [(18,69),(77,69),(20,7),(75,7)]:
        fill(x,4,z,x,10,z,'jungle_log',{'axis':'y'})
        for dx,dz in [(0,0),(-1,0),(1,0),(0,-1),(0,1),(-2,0),(2,0),(0,-2),(0,2)]:
            setb(x+dx,11-(abs(dx)+abs(dz)>1),z+dz,'jungle_leaves',{'distance':'1','persistent':'true','waterlogged':'false'})
    for z in range(12,72,8):lamp(44,4,z);lamp(52,4,z)
    for i,(x,z) in enumerate([(32,23),(63,23),(30,62),(65,62)]):djinn(x,4,z,['blue','red','white','black'][i],'mage',4)
    for i,(x,z) in enumerate([(44,13),(52,13),(43,65),(53,65),(7,44),(89,44)]):djinn(x,4,z,['blue','red','white','black'][i%4],'guard',8)
    # Serialize the vanilla structure template.
    palette=[];lookup={};encoded=[]
    for pos,state in sorted(blocks.items()):
        if state not in lookup:
            lookup[state]=len(palette)
            entry={'Name':(8,'minecraft:'+state[0])}
            if state[1]:entry['Properties']=(10,{k:(8,v) for k,v in state[1]})
            palette.append(entry)
        entry={'pos':(9,(3,list(pos))),'state':(3,lookup[state])}
        if pos in block_entities:entry['nbt']=(10,block_entities[pos])
        encoded.append(entry)
    tree={'DataVersion':(3,3955),'size':(9,(3,[97,34,97])),'palette':(9,(10,palette)),
          'blocks':(9,(10,encoded)),'entities':(9,(10,residents))}
    path=D/'structure/djin_city_capital.nbt';path.parent.mkdir(parents=True,exist_ok=True)
    path.write_bytes(gzip.compress(b'\x0a\x00\x00'+nbt_payload(10,tree),mtime=0))
    write_json(D/'worldgen/template_pool/djin_city_capital/start.json',{'name':'djinns:djin_city_capital/start','fallback':'minecraft:empty',
       'elements':[{'weight':1,'element':{'element_type':'minecraft:single_pool_element','location':'djinns:djin_city_capital',
                                        'processors':'minecraft:empty','projection':'rigid'}}]})
    write_json(D/'worldgen/structure/djin_city_capital.json',{
       'type':'minecraft:jigsaw','biomes':'#djinns:has_structure/djin_city_capital','step':'surface_structures',
       'spawn_overrides':{},'terrain_adaptation':'beard_thin','start_pool':'djinns:djin_city_capital/start',
       'size':0,'start_height':{'absolute':0},'project_start_to_heightmap':'WORLD_SURFACE_WG',
       'max_distance_from_center':112,'use_expansion_hack':False})
    write_json(D/'worldgen/structure_set/djin_city_capital.json',{'structures':[{'structure':'djinns:djin_city_capital','weight':1}],
       'placement':{'type':'minecraft:random_spread','spacing':36,'separation':12,'salt':73110429}})
    write_json(D/'tags/worldgen/biome/has_structure/djin_city_capital.json',{'replace':False,'values':['minecraft:desert']})
    write_json(D/'loot_table/chests/capital.json',{'type':'minecraft:chest','pools':[{'rolls':{'type':'minecraft:uniform','min':3,'max':6},
       'entries':[{'type':'minecraft:item','name':name,'weight':weight,'functions':[{'function':'minecraft:set_count','count':{'type':'minecraft:uniform','min':lo,'max':hi}}]}
          for name,weight,lo,hi in [('minecraft:lapis_lazuli',10,5,18),('minecraft:gold_ingot',5,2,7),('minecraft:bread',7,3,8),
          ('minecraft:emerald',3,1,3),('djinns:occult_scroll_wind',2,1,1),('djinns:occult_scroll_ward',1,1,1)]]}]})
    function=D/'function/place_capital.mcfunction';function.parent.mkdir(parents=True,exist_ok=True)
    function.write_text('# Stand in an open desert area; places a 97 x 97 block structure.\nplace structure djinns:djin_city_capital ~ ~ ~\n')
    write_json(ROOT/'tools/update/capital_stats.json',{'blocks':len(blocks),'palette':len(palette),'residents':len(residents),
               'kings':1,'size':[97,34,97]})
    # Top-down plan from the real placed blocks, for layout inspection.
    color={'smooth_sandstone':(216,194,145),'sandstone':(184,160,108),'cut_sandstone':(196,177,132),
           'chiseled_sandstone':(175,148,97),'water':(38,124,168),'warped_planks':(27,112,121),
           'dark_prismarine':(28,79,78),'gold_block':(236,186,58),'red_carpet':(128,35,50),'red_wool':(128,35,50),
           'cyan_wool':(25,135,150),'white_wool':(224,219,195),'jungle_leaves':(50,106,61),'sea_lantern':(165,240,224)}
    plan=Image.new('RGB',(97,97),(92,77,58))
    tops={}
    for (x,y,z),state in blocks.items():
        if state[0]!='air' and y>tops.get((x,z),(-1,''))[0]:tops[(x,z)]=(y,state[0])
    for (x,z),(y,name) in tops.items():
        base=color.get(name,(147,124,85));plan.putpixel((x,z),tuple(min(255,int(c*(.85+y*.009))) for c in base))
    (ROOT/'build/previews').mkdir(parents=True,exist_ok=True)
    plan.resize((776,776),Image.Resampling.NEAREST).save(ROOT/'build/previews/capital-plan.png')
    print('Capital:',len(blocks),'blocks,',len(residents),'djinns including one king; NBT',path.stat().st_size,'bytes')

if __name__=='__main__':
    parts=geometry();royal_texture();elements=sword();city()
    print('Royal geometry:',len(parts),'new articulated/decorative parts; Riftblade:',len(elements),'voxel elements')
