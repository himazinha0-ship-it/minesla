"""Package only the mod and plain-text documentation; no build tools or fixtures."""
from pathlib import Path
import hashlib,zipfile
ROOT=Path(__file__).resolve().parents[2];DIST=ROOT/'dist'
name='djinns-fabric-1.21.1-1.1.1';jar=DIST/(name+'.jar')
readme=DIST/'LEIA-ME-1.1.1.txt'
readme.write_text('''DJINNS 1.1.1 - FABRIC - MINECRAFT JAVA 1.21.1

Feche o jogo e mova o Djinns antigo para fora de mods. Extraia este ZIP e
coloque somente djinns-fabric-1.21.1-1.1.1.jar em mods. Mantenha Fabric API
para 1.21.1, Fabric Loader 0.16.14+ e Java 21. Use a mesma versao no servidor.

CORRECOES
- Busca da capital entre ticks, cancelavel e limitada a 30 segundos ou 128
  candidatos; coordenadas somente depois de confirmar o inicio da estrutura.
- Espada alinhada ao centro da mao, com a lamina para cima.
- Rei invoca ate tres guardas armados por onda, alternando quatro cores,
  procurando chao livre. Limite de seis invocados ativos proximos.
- Navegacao terrestre e gravidade para manter os djinns junto ao chao,
  preservando a animacao visual de flutuacao, inclusive nos djinns ja salvos.

BUSCAR A CIDADE
/locate structure djinns:djin_city_capital
ou /djinns locate
Para interromper: /djinns cancelar
Sem resultado dentro do limite, aproxime-se de um deserto e tente novamente.
A vila surge naturalmente em chunks novos de deserto. A busca nao garante
a cidade mais proxima de todo o mundo.

TESTAR O REI
Em Sobrevivencia, ataque o rei e aguarde cerca de tres segundos a 20 TPS.
As fendas precisam de chao livre. O rei ignora jogadores em Criativo.

Os testes automatizados passaram, mas este ambiente nao executou um cliente
ou servidor Minecraft completo. Leia VALIDACAO-1.1.1.txt para os limites.
Este pacote contem somente o mod e documentos; nao inclui instalador.
''',encoding='utf-8')
validation=DIST/'VALIDACAO-1.1.1.txt';validation.write_text((ROOT/'VALIDACAO.md').read_text(),encoding='utf-8')
def checksum(path):
 value=hashlib.sha256(path.read_bytes()).hexdigest()
 path.with_suffix(path.suffix+'.sha256').write_text(value+'  '+path.name+'\n');return value
jarhash=checksum(jar);archive=DIST/(name+'.zip')
with zipfile.ZipFile(archive,'w',compression=zipfile.ZIP_DEFLATED,compresslevel=9) as z:
 for p in [jar,jar.with_suffix('.jar.sha256'),readme,validation]:
  info=zipfile.ZipInfo(p.name,date_time=(2026,10,28,0,0,0));info.compress_type=zipfile.ZIP_DEFLATED;info.external_attr=0o100644<<16
  z.writestr(info,p.read_bytes())
ziphash=checksum(archive)
with zipfile.ZipFile(archive) as z:
 assert z.testzip() is None and z.read(jar.name)==jar.read_bytes()
print('JAR',jar.stat().st_size,jarhash);print('ZIP',archive.stat().st_size,ziphash)
