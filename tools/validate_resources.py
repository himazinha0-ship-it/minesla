"""Static integrity checks. These do not replace a Fabric build or in-game tests."""
from pathlib import Path
import json
import re
import struct
import zlib
import zipfile
import argparse

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'src/main/resources'
ASSETS = RES / 'assets/djinns'
DATA = RES / 'data/djinns'

def require(condition, message):
    if not condition:
        raise AssertionError(message)

documents = {}
for path in sorted(RES.rglob('*.json')):
    documents[path] = json.loads(path.read_text(encoding='utf8'))

def png_size(path):
    content = path.read_bytes()
    require(content[:8] == b'\x89PNG\r\n\x1a\n', f'Invalid PNG signature: {path}')
    offset, width, height, has_end, compressed = 8, 0, 0, False, bytearray()
    while offset < len(content):
        length = struct.unpack('>I', content[offset:offset + 4])[0]
        kind = content[offset + 4:offset + 8]
        data = content[offset + 8:offset + 8 + length]
        crc = struct.unpack('>I', content[offset + 8 + length:offset + 12 + length])[0]
        require(zlib.crc32(kind + data) & 0xffffffff == crc, f'PNG CRC error: {path}')
        if kind == b'IHDR': width, height = struct.unpack('>II', data[:8])
        if kind == b'IDAT': compressed.extend(data)
        if kind == b'IEND': has_end = True
        offset += length + 12
    require(width > 0 and height > 0 and has_end, f'Incomplete PNG: {path}')
    require(bool(zlib.decompress(compressed)), f'Empty PNG image: {path}')
    return width, height

pngs = list(ASSETS.rglob('*.png'))
for path in pngs: png_size(path)

for path, doc in documents.items():
    if 'models' not in path.parts:
        continue
    for ref in [doc.get('parent', '')] + [o['model'] for o in doc.get('overrides', [])]:
        if ref.startswith('djinns:'):
            target = ASSETS / ('models/' + ref.split(':')[1] + '.json')
            require(target.is_file(), f'{path.name}: missing model {ref}')
    for ref in doc.get('textures', {}).values():
        if ref.startswith('djinns:'):
            target = ASSETS / ('textures/' + ref.split(':')[1] + '.png')
            require(target.is_file(), f'{path.name}: missing texture {ref}')

names = [f'{c}_{r}_djinn' for c in ['blue', 'red', 'white', 'black']
         for r in ['common', 'merchant', 'guard', 'mage']] + ['djinn_king']
languages = {lang: json.loads((ASSETS / f'lang/{lang}.json').read_text(encoding='utf8'))
             for lang in ['pt_br', 'en_us']}
require(languages['pt_br'].keys() == languages['en_us'].keys(), 'Missing locale keys')
for name in names:
    require((DATA / f'loot_table/entities/{name}.json').is_file(), f'Missing loot: {name}')
    require((ASSETS / f'models/item/{name}_spawn_egg.json').is_file(), f'Missing egg model: {name}')
    texture = 'king' if name == 'djinn_king' else name.removesuffix('_djinn')
    require(png_size(ASSETS / f'textures/entity/{texture}.png') == (128, 128), f'Missing/invalid skin: {name}')
    for locale in languages.values():
        for key in [f'entity.djinns.{name}', f'item.djinns.{name}_spawn_egg']:
            require(key in locale, f'Missing translation: {key}')

main_source = (ROOT / 'src/main/java/dev/minesla/djinn/Djinns.java').read_text()
item_names = [name for name in re.findall(r'item\("([a-z_]+)"', main_source) if not name.endswith('_spawn_egg')]
require(len(item_names) == 12, 'Expected 12 equipment/scroll items')
for name in item_names:
    require((ASSETS / f'models/item/{name}.json').is_file(), f'Missing item model: {name}')
    require('item.djinns.' + name in languages['pt_br'], f'Missing item translation: {name}')
for path in (DATA / 'recipe').glob('*.json'):
    doc = documents[path]
    require(doc['result']['id'].removeprefix('djinns:') in item_names, f'Unknown result: {path}')
    symbols = set(''.join(doc['pattern'])) - {' '}
    require(symbols == set(doc['key']), f'Recipe key mismatch: {path}')
    require(len(set(map(len, doc['pattern']))) == 1, f'Uneven recipe: {path}')

manifest = documents[RES / 'fabric.mod.json']
for side, entries in manifest['entrypoints'].items():
    for entry in entries:
        source_set = 'client' if side == 'client' else 'main'
        require((ROOT / f'src/{source_set}/java' / (entry.replace('.', '/') + '.java')).is_file(), f'Missing entrypoint: {entry}')
for path in (ROOT / 'src/main/java').rglob('*.java'):
    require('net.minecraft.client.' not in path.read_text(), f'Client dependency on dedicated server: {path}')
require((DATA / 'structure/djin_city_capital.nbt').is_file(), 'Missing desert capital template')
require((DATA / 'worldgen/structure/djin_city_capital.json').is_file(), 'Missing capital structure registration')

print(f'PASS: {len(documents)} JSON documents, {len(pngs)} PNG files with CRC/zlib checks, '
      f'17 entities, 12 equipment/scroll items, 7 recipes, 2 matching locales.')
print('Static checks only. Minecraft compilation and gameplay remain unverified.')

parser = argparse.ArgumentParser()
parser.add_argument('--jar', type=Path, help='Additionally check the actual remapped mod JAR, not the sources JAR')
options = parser.parse_args()
if options.jar:
    with zipfile.ZipFile(options.jar) as jar:
        config = json.loads(jar.read('djinns.client.mixins.json'))
        require(bool(config.get('refmap')), 'Missing refmap declaration: this causes the reported world-entry failure')
        require(config['refmap'] in jar.namelist(), 'Declared refmap is absent from the installed JAR')
        refmap = json.loads(jar.read(config['refmap']))
        selectors = refmap.get('mappings', {}).get('dev/minesla/djinn/mixin/KeyboardInputMixin', {})
        target = selectors.get('tick(ZF)V') or selectors.get('tick') or ''
        require('method_3129(ZF)V' in target, 'KeyboardInput.tick was not mapped for production 1.21.1')
        print('PASS: installed JAR includes the client mixin refmap and production tick selector.')
