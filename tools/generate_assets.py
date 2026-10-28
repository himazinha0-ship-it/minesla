"""Rebuild original pixel art and data assets. Requires Pillow only for asset development."""
from pathlib import Path
from PIL import Image, ImageDraw
import json
import random

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'
A = ROOT / 'assets/djinns'
D = ROOT / 'data/djinns'
COLORS = {'blue': (43, 62, 99), 'red': (129, 39, 38), 'white': (184, 202, 213), 'black': (34, 28, 48)}
ROLES = ['common', 'merchant', 'guard', 'mage']
GOLD = (208, 160, 64)
CREAM = (186, 168, 142)

def save_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + '\n', encoding='utf8')

def noisy_region(im, box, rgb, seed, spread=14):
    rnd = random.Random(seed)
    for y in range(box[1], box[3]):
        for x in range(box[0], box[2]):
            noise = rnd.randint(-spread, spread) + (5 if (y // 3) % 2 == 0 else -2)
            im.putpixel((x, y), tuple(max(0, min(255, c + noise)) for c in rgb) + (255,))

for index, (color, rgb) in enumerate(COLORS.items()):
    for role in ROLES:
        im = Image.new('RGBA', (128, 128), (*rgb, 255))
        noisy_region(im, (0, 0, 64, 32), rgb, index)
        noisy_region(im, (0, 32, 32, 64), tuple(int(c * .78) for c in rgb), index + 10)
        noisy_region(im, (32, 32, 64, 64), CREAM, 21)
        noisy_region(im, (64, 0, 96, 32), tuple(min(255, c + 20) for c in rgb), index + 20)
        noisy_region(im, (64, 32, 96, 64), GOLD, 31)
        noisy_region(im, (96, 32, 128, 64), tuple(min(255, c + 35) for c in rgb), 41)
        sash = {'common': (104, 38, 65), 'merchant': (40, 101, 89), 'guard': (113, 61, 37), 'mage': (93, 47, 126)}[role]
        noisy_region(im, (64, 64, 128, 96), sash, 51)
        draw = ImageDraw.Draw(im)
        for y in (36, 40, 44, 49, 54, 60): draw.line((32, y, 63, y), fill=(149, 131, 110))
        for x in (65, 72, 79, 86, 93): draw.line((x, 33, x, 62), fill=(233, 190, 96))
        draw.rectangle((0, 96, 8, 101), fill=(255, 207, 91))
        draw.rectangle((32, 96, 40, 106), fill=((57, 145, 233) if color != 'red' else (255, 98, 38)))
        draw.rectangle((33, 97, 34, 103), fill=(125, 221, 255))
        # The overlaid chest panel distinguishes guard chain armor and mage runes.
        if role == 'guard':
            for y in range(33, 62, 3):
                for x in range(96 + (y % 2), 126, 3): draw.point((x, y), fill=GOLD)
        im.save(A / f'textures/entity/{color}_{role}.png')

king = Image.open(A / 'textures/entity/blue_guard.png').copy()
noisy_region(king, (0, 0, 64, 32), (25, 49, 65), 87)
noisy_region(king, (64, 0, 96, 32), (17, 153, 174), 88)
noisy_region(king, (64, 64, 128, 96), (202, 187, 150), 89)
ImageDraw.Draw(king).rectangle((32, 96, 40, 106), fill=(22, 225, 229))
king.save(A / 'textures/entity/king.png')
for name, color in [('eyes', (255, 210, 100)), ('king_eyes', (35, 255, 240))]:
    im = Image.new('RGBA', (128, 128))
    ImageDraw.Draw(im).rectangle((0, 96, 8, 101), fill=color)
    im.save(A / f'textures/entity/{name}.png')

armor_dir = A / 'textures/models/armor'
armor_dir.mkdir(parents=True, exist_ok=True)
for layer in (1, 2):
    im = Image.new('RGBA', (64, 32), (*CREAM, 255))
    noisy_region(im, (0, 0, 64, 32), CREAM, 72 + layer, 8)
    draw = ImageDraw.Draw(im)
    for y in (2, 7, 14, 19, 26, 30): draw.rectangle((0, y, 63, y + 1), fill=GOLD)
    for x in (1, 15, 28, 40, 54): draw.line((x, 0, x, 31), fill=(139, 111, 67))
    draw.rectangle((25, 20, 29, 24), fill=(31, 135, 177))
    im.save(armor_dir / f'desert_layer_{layer}.png')

shield = Image.new('RGBA', (64, 64), (120, 84, 41, 255))
sd = ImageDraw.Draw(shield)
sd.rectangle((0, 0, 25, 24), fill=GOLD)
sd.rectangle((2, 2, 23, 22), fill=(34, 51, 77))
sd.rectangle((10, 3, 14, 21), fill=GOLD)
sd.rectangle((3, 10, 22, 14), fill=GOLD)
sd.polygon([(12, 6), (18, 12), (12, 18), (6, 12)], fill=(36, 167, 210))
shield.save(A / 'textures/entity/desert_shield.png')

def sprite(name, kind, color=None):
    im = Image.new('RGBA', (32, 32)); d = ImageDraw.Draw(im)
    if kind in ('sword', 'spear'):
        d.line((5, 27, 13, 19), fill=(91, 58, 32), width=3)
        d.line((7, 18, 15, 26), fill=GOLD, width=3)
        if kind == 'spear':
            d.line((4, 28, 23, 9), fill=(143, 93, 42), width=3)
            d.polygon([(19, 10), (29, 2), (26, 13), (22, 15)], fill=(218, 225, 206))
            d.line((22, 12, 28, 3), fill=(255, 255, 234))
        else:
            blade = color or (255, 128, 29)
            d.polygon([(12, 19), (25, 3), (29, 1), (29, 6), (16, 22)], fill=blade)
            d.line((14, 19, 28, 3), fill=(204, 255, 240) if 'royal' in name else (255, 236, 138), width=2)
            for x, y in [(18, 12), (23, 6), (15, 16), (26, 3)]:
                d.rectangle((x - 3, y - 3, x - 2, y - 1), fill=blade)
        d.rectangle((3, 26, 6, 29), fill=GOLD)
        d.point((11, 21), fill=(54, 188, 240))
    elif kind == 'scroll':
        d.rectangle((6, 4, 25, 27), fill=(120, 91, 58))
        d.rectangle((8, 5, 24, 26), fill=(221, 203, 159))
        d.rectangle((5, 3, 25, 6), fill=(245, 224, 184))
        d.rectangle((5, 26, 25, 29), fill=(180, 144, 85))
        for y in (10, 13, 21): d.line((11, y, 21, y), fill=(114, 88, 64))
        d.polygon([(16, 13), (21, 18), (16, 23), (11, 18)], fill=color)
        d.rectangle((15, 16, 17, 20), fill=(255, 238, 178))
    elif kind == 'helmet':
        d.rectangle((5, 9, 26, 22), fill=GOLD)
        d.rectangle((7, 6, 24, 15), fill=CREAM)
        d.rectangle((10, 17, 21, 24), fill=(0, 0, 0, 0))
        d.rectangle((14, 8, 17, 15), fill=(29, 152, 213))
    elif kind == 'chestplate':
        d.polygon([(8, 5), (12, 8), (19, 8), (23, 5), (29, 12), (24, 16), (23, 27), (8, 27), (7, 16), (2, 12)], fill=CREAM)
        d.rectangle((8, 21, 23, 24), fill=GOLD)
        d.line((10, 9, 21, 19), fill=GOLD, width=3)
    elif kind == 'leggings':
        d.rectangle((7, 5, 24, 11), fill=GOLD)
        d.rectangle((8, 12, 14, 28), fill=CREAM); d.rectangle((18, 12, 24, 28), fill=CREAM)
    elif kind == 'boots':
        d.rectangle((6, 9, 13, 25), fill=CREAM); d.rectangle((19, 9, 26, 25), fill=CREAM)
        d.rectangle((3, 23, 13, 28), fill=GOLD); d.rectangle((17, 23, 27, 28), fill=GOLD)
    im.save(A / f'textures/item/{name}.png')
    save_json(A / f'models/item/{name}.json', {'parent': 'minecraft:item/handheld' if kind in ('sword', 'spear') else 'minecraft:item/generated', 'textures': {'layer0': f'djinns:item/{name}'}})

sprite('desert_spear', 'spear')
sprite('djinn_flame_sword', 'sword')
sprite('royal_sword', 'sword', (18, 213, 216))
for name, color in [('flame', (230, 83, 27)), ('wind', (132, 200, 216)), ('shadow', (93, 45, 145)), ('ward', (35, 169, 137))]:
    sprite(f'occult_scroll_{name}', 'scroll', color)
for name in ('helmet', 'chestplate', 'leggings', 'boots'): sprite(f'desert_{name}', name)
save_json(A / 'models/item/desert_shield.json', {
    'parent': 'minecraft:item/shield',
    'textures': {'particle': 'djinns:entity/desert_shield'},
    'overrides': [{'predicate': {'blocking': 1}, 'model': 'djinns:item/desert_shield_blocking'}]})
save_json(A / 'models/item/desert_shield_blocking.json', {'parent': 'minecraft:item/shield_blocking'})

pt = {
    'itemGroup.djinns': 'Djinns: Espíritos do Deserto',
    'effect.djinns.possession': 'Possessão Djinn',
    'message.djinns.possessed': 'Sua vontade foi tomada por um djinn por 5 segundos!',
    'message.djinns.profession': 'Profissão: %s',
    'entity.djinns.djinn_king': 'Rei dos Djinns',
    'item.djinns.djinn_king_spawn_egg': 'Ovo gerador de Rei dos Djinns',
}
en = {
    'itemGroup.djinns': 'Djinns: Spirits of the Desert',
    'effect.djinns.possession': 'Djinn Possession',
    'message.djinns.possessed': 'A djinn has taken your will for 5 seconds!',
    'message.djinns.profession': 'Profession: %s',
    'entity.djinns.djinn_king': 'Djinn King',
    'item.djinns.djinn_king_spawn_egg': 'Djinn King Spawn Egg',
}
pt_colors = {'blue': 'Azul', 'red': 'Vermelho', 'white': 'Branco', 'black': 'Negro'}
pt_roles = {'common': 'Comum', 'merchant': 'Comerciante', 'guard': 'Guarda', 'mage': 'Mago'}
for color in COLORS:
    for role in ROLES:
        name = f'{color}_{role}_djinn'
        pt[f'entity.djinns.{name}'] = f'Djinn {pt_colors[color]} {pt_roles[role]}'
        en[f'entity.djinns.{name}'] = f'{color.title()} {role.title()} Djinn'
        pt[f'item.djinns.{name}_spawn_egg'] = f'Ovo gerador de {pt[f"entity.djinns.{name}"]}'
        en[f'item.djinns.{name}_spawn_egg'] = en[f'entity.djinns.{name}'] + ' Spawn Egg'
for name in [f'{c}_{r}_djinn' for c in COLORS for r in ROLES] + ['djinn_king']:
    save_json(A / f'models/item/{name}_spawn_egg.json', {'parent': 'minecraft:item/template_spawn_egg'})
    save_json(D / f'loot_table/entities/{name}.json', {
        'type': 'minecraft:entity', 'pools': [{
            'rolls': 1,
            'entries': [{'type': 'minecraft:item', 'name': 'minecraft:lapis_lazuli'}],
            'functions': [{'function': 'minecraft:set_count', 'count': {'type': 'minecraft:uniform', 'min': 12 if name == 'djinn_king' else 1, 'max': 24 if name == 'djinn_king' else 3}}],
            'conditions': [{'condition': 'minecraft:killed_by_player'}]
        }]})

names = {
    'desert_spear': ('Lança do Deserto', 'Desert Spear'),
    'djinn_flame_sword': ('Espada Flamejante Djinn', 'Djinn Flame Sword'),
    'royal_sword': ('Lâmina das Fendas do Rei', 'King’s Riftblade'),
    'desert_shield': ('Escudo do Deserto', 'Desert Shield'),
    'desert_helmet': ('Turbante do Deserto', 'Desert Turban'),
    'desert_chestplate': ('Peitoral do Deserto', 'Desert Chestplate'),
    'desert_leggings': ('Calças do Deserto', 'Desert Leggings'),
    'desert_boots': ('Botas do Deserto', 'Desert Boots'),
    'occult_scroll_flame': ('Pergaminho Oculto: Chama', 'Occult Scroll: Flame'),
    'occult_scroll_wind': ('Pergaminho Oculto: Vento', 'Occult Scroll: Wind'),
    'occult_scroll_shadow': ('Pergaminho Oculto: Sombra', 'Occult Scroll: Shadow'),
    'occult_scroll_ward': ('Pergaminho Oculto: Proteção', 'Occult Scroll: Ward'),
}
for item, (p, e) in names.items(): pt['item.djinns.' + item] = p; en['item.djinns.' + item] = e
jobs = ['farmer', 'fisherman', 'shepherd', 'fletcher', 'librarian', 'cartographer', 'cleric', 'armorer', 'weaponsmith', 'toolsmith', 'butcher', 'leatherworker', 'mason']
pt_jobs = ['Fazendeiro', 'Pescador', 'Pastor', 'Flecheiro', 'Bibliotecário', 'Cartógrafo', 'Clérigo', 'Armeiro', 'Ferreiro de armas', 'Ferreiro de ferramentas', 'Açougueiro', 'Curtidor', 'Pedreiro']
for job, p in zip(jobs, pt_jobs): pt['profession.djinns.' + job] = p; en['profession.djinns.' + job] = job.title()
save_json(A / 'lang/pt_br.json', pt); save_json(A / 'lang/en_us.json', en)

recipes = {
    'desert_spear': (['  I', ' S ', 'S  '], {'I': 'iron_ingot', 'S': 'stick'}),
    'djinn_flame_sword': ([' B ', ' G ', ' S '], {'B': 'blaze_rod', 'G': 'gold_ingot', 'S': 'stick'}),
    'desert_shield': (['GLG', 'GSG', ' G '], {'G': 'gold_ingot', 'L': 'lapis_lazuli', 'S': 'shield'}),
    'desert_helmet': (['WWW', 'G G'], {'W': 'white_wool', 'G': 'gold_ingot'}),
    'desert_chestplate': (['G G', 'WLW', 'WWW'], {'G': 'gold_ingot', 'W': 'white_wool', 'L': 'lapis_lazuli'}),
    'desert_leggings': (['GGG', 'W W', 'W W'], {'G': 'gold_ingot', 'W': 'white_wool'}),
    'desert_boots': (['W W', 'G G'], {'G': 'gold_ingot', 'W': 'white_wool'}),
}
for name, (pattern, key) in recipes.items():
    save_json(D / f'recipe/{name}.json', {'type': 'minecraft:crafting_shaped', 'category': 'equipment',
        'pattern': pattern, 'key': {k: {'item': 'minecraft:' + v} for k, v in key.items()}, 'result': {'id': 'djinns:' + name, 'count': 1}})

print('Generated 17 entity loot tables, 17 skins, equipment textures, item models, 7 recipes and PT-BR/EN translations.')
