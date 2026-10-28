"""Refresh normal Loom sources from the pinned-intermediary incremental sources.
Usage: python tools/fixes/named_sources.py /path/to/yarn/mappings
Yarn: 1.21.1+build.3 (official FabricMC/yarn sources).
"""
from pathlib import Path
import re,sys
ROOT=Path(__file__).resolve().parents[2]
names={}
for path in Path(sys.argv[1]).rglob('*.mapping'):
 stack=[]
 for line in path.read_text().splitlines():
  fields=line.split();depth=len(line)-len(line.lstrip('\t'))
  if not fields:continue
  if fields[0]=='CLASS' and len(fields)>=3:
   while stack and stack[-1][0]>=depth:stack.pop()
   intermediary,named=fields[1:3]
   if '/' not in intermediary:
    if not stack:continue
    intermediary=stack[-1][1]+'$'+intermediary;named=stack[-1][2]+'.'+named
   else:named=named.replace('/','.')
   stack.append((depth,intermediary,named));names[intermediary.removeprefix('net/minecraft/')]=named
  elif fields[0] in ('METHOD','FIELD') and len(fields)==4:names[fields[1]]=fields[2]
# These generated vanilla constant names are absent from Yarn's editable mapping files.
# Their intermediary references were retained from the user's compiled original JAR.
names.update(field_22246='SOUL_FIRE_FLAME',field_15193='ENTITY_EVOKER_PREPARE_SUMMON',field_16471='MOB_SUMMONED')
destination=ROOT/'src/main/java/dev/minesla/djinn/runtime';destination.mkdir(parents=True,exist_ok=True)
for source in (ROOT/'tools/fixes/runtime').glob('*.java'):
 text=source.read_text().replace('import net.minecraft.*;\n','')
 text=text.replace('/** Intermediary API names are deliberate: this is compiled for the installed 1.21.1 JAR. */',
                   '/** Bounded server-tick search; see tools/fixes for the incremental distribution build. */')
 def replace(match):
  token=match.group()
  if token not in names:raise AssertionError('Unmapped Minecraft symbol: '+token)
  return names[token]
 text=re.sub(r'\b(?:class_\d+(?:\$class_\d+)*|method_\d+|field_\d+)\b',replace,text)
 (destination/source.name).write_text(text)
print('Refreshed 3 named runtime sources for the full Loom build.')
