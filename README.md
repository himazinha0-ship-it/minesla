# Djinns: Espíritos do Deserto — Fabric 1.21.1

Projeto para **Minecraft Java 1.21.1**, Fabric Loader **0.16.14+**, Fabric API
**0.116.6+1.21.1** e **JDK 21**. Instale no cliente e no servidor.
Não é para Bedrock, Forge ou NeoForge.

**Somente mobs e itens. Não gera cidades, palácios nem `djin_city_capital`,
conforme a correção de escopo.**

## Estado da entrega

### Correção de entrada no mundo — 1.0.1

Os seis relatórios enviados confirmaram `KeyboardInputMixin`: o seletor `tick`
ficou sem o refmap que o traduz para `method_3129(ZF)V` no Minecraft instalado.
A mensagem de desconexão é consequência dessa falha de carregamento do cliente.
A versão 1.0.1 configura o processador de mixins para as fontes de cliente,
declara `djinns.client.refmap.json` e verifica esse mapa no JAR de distribuição
durante `build`. A possessão continua implementada.

O corretor separado foi retirado do link de distribuição após um alerta do
Windows Defender relatado pelo usuário. A causa desse alerta não foi confirmada.
Não é necessário executar um corretor nem alterar a proteção do Windows.
Para reparar diretamente a instalação afetada, é necessário fornecer o JAR
original do mod. Os arquivos recebidos até agora são relatórios de desconexão;
o projeto-fonte não contém o JAR compilado usado no computador do usuário.

Código, modelos Java, texturas originais, receitas, loot e traduções incluídos.
**A compilação e os testes no Minecraft ainda não foram concluídos:** a política
de rede bloqueou `maven.fabricmc.net` com HTTP 403 ao resolver Fabric Loom.
O ZIP de projeto **não deve ser colocado na pasta `mods`**; ainda não há um JAR
validado ou garantia de funcionamento em jogo. Veja `VALIDACAO.md`.

## Compilar e instalar

1. Instale **JDK 21 completo**. Confirme `java -version` e `javac -version`;
   configure `JAVA_HOME` para esse JDK.
2. Extraia o projeto e abra um terminal na pasta de `build.gradle`.
3. Linux/macOS: `chmod +x gradlew && ./gradlew build`.
   Windows: `gradlew.bat build`.
4. Após um build bem-sucedido, copie
   `build/libs/djinns-fabric-1.21.1-1.0.1.jar` para `.minecraft/mods`.
   Não use o arquivo `-sources.jar`.
5. Instale Fabric Loader 0.16.14+ para Minecraft **1.21.1** e
   [Fabric API para 1.21.1](https://modrinth.com/mod/fabric-api/versions?g=1.21.1).
6. Em multiplayer, instale os dois mods também no servidor Fabric.

O wrapper confere o SHA-256 do Gradle. Preserve TLS e checksums.
Desenvolvimento: `./gradlew runClient` ou `./gradlew runServer`. A EULA do
servidor precisa ser aceita pelo administrador em `run/eula.txt`.
Não há dependência de GeckoLib. Pillow só é necessário para regenerar a arte
com `python3 tools/generate_assets.py`; não é necessário para compilar ou jogar.

## Funcionalidades implementadas no código

- **16 djinns**: quatro cores × comum, comerciante, guarda e mago, mais o rei.
- **Azul:** combate físico; o mago aplica Fraqueza à distância.
- **Vermelho:** fogo ao acertar e bolas de fogo reais de blaze à distância.
- **Branco:** Levitação por 3 segundos ao acertar.
- **Negro:** Lentidão II e Escuridão por 5 segundos ao acertar.
- Todos flutuam, não sofrem dano de queda e começam neutros. Vermelhos resistem
  a fogo. Ao receber dano, chamam aliados de todas as cores até 32 blocos.
  A hostilidade dura 30 s, renovável por dano. Criativo e Espectador são imunes.
- Um golpe corpo a corpo de jogador que cause dano tem **15% de chance** de
  causar **Possessão por 100 ticks (5 s a 20 TPS)**, Cegueira e Náusea.
  O cliente substitui o movimento; o servidor força ataques contra criaturas
  visíveis aleatórias até 2,8 blocos. Os ataques involuntários não atingem
  jogadores. Projéteis não causam possessão; o efeito não se acumula.
- Guardas portam lança ou espada flamejante, escudo e peitoral do deserto.
  Têm 25% de chance de bloquear dano vindo pela frente.
- Comuns, comerciantes e magos abrem a interface vanilla de trocas por
  **lápis-lazúli**. Guardas e o rei não negociam. Há 13 profissões inspiradas nos
  aldeões, níveis 1–5 e reposição diária. Novas ofertas de nível aparecem após
  fechar e reabrir a tela. Antes da primeira troca, clique segurando uma bancada
  para mudar de profissão, sem consumir o bloco. Magos não mudam de profissão.
  Não há simulação completa de aldeia, reprodução ou reputação.
- O rei tem **300 de vida**, barra de chefe, espada ciano flamejante, golpes em
  arco com recuo e animações interpoladas. Fendas verticais de partículas
  invocam guardas após 1,5 s de preparação. Até seis invocados ficam próximos;
  desaparecem após 60 s, sem loot/XP. Abaixo de meia vida, as fendas aceleram.
- Arte original inspirada nas referências: cauda segmentada animada, olhos
  emissivos, turbantes, braceletes, faixas por papel e ombreiras do rei.
  É uma interpretação em blocos, não uma reprodução exata das imagens.

## Encontrar e invocar

As 16 variantes entram na geração de criaturas dos desertos, sobre areia e
com luz acima de 8. A aparição depende dos limites vanilla de criaturas.
Sem cidade nesta versão, o rei é acessível por ovo criativo ou comando.
Todos os itens e ovos estão na aba **Djinns: Espíritos do Deserto**.

```mcfunction
/summon djinns:blue_common_djinn ~ ~1 ~
/summon djinns:red_guard_djinn ~ ~1 ~
/summon djinns:white_merchant_djinn ~ ~1 ~
/summon djinns:black_mage_djinn ~ ~1 ~
/summon djinns:djinn_king ~ ~1 ~
/function djinns:kit
/function djinns:showcase
```

Use `blue`, `red`, `white` ou `black` e `common`, `merchant`, `guard` ou `mage`
para obter os demais IDs. `showcase` invoca as 16 variantes numa grade sem
construir blocos nem invocar o rei. `kit` fornece equipamentos e lápis-lazúli.
Use uma área aberta num mundo de testes.

## Profissões

| Profissão | Bancada segurada | Exemplos de ofertas |
|---|---|---|
| Fazendeiro | Composteira | Pão, cenoura dourada |
| Pescador | Barril | Peixe cozido, vara |
| Pastor | Tear | Lã, cama |
| Flecheiro | Mesa de arquearia | Flechas, arco |
| Bibliotecário | Atril | Livros, estantes |
| Cartógrafo | Mesa de cartografia | Mapas, bússola |
| Clérigo | Suporte de poções | Redstone, pedra luminosa |
| Armeiro | Alto-forno | Turbante, botas |
| Ferreiro de armas | Rebolo | Lança, espada |
| Ferreiro de ferramentas | Mesa de ferraria | Picareta, machado |
| Açougueiro | Defumador | Carnes cozidas |
| Curtidor | Caldeirão | Couro, sela |
| Pedreiro | Cortador de pedras | Arenito, terracota |

Mestres desbloqueiam equipamentos e pergaminhos. Magos são bibliotecários
arcanos com um catálogo próprio.

## Pergaminhos e equipamento

Botão direito usa e consome um pergaminho, exceto no Criativo. Intervalo de
5 segundos por tipo.

| Pergaminho | Efeito |
|---|---|
| Chama | Resistência ao fogo por 60 s; incendeia criaturas visíveis até 5 blocos |
| Vento | Levitação por 3 s e Queda lenta por 20 s |
| Sombra | Invisibilidade e Visão noturna por 30 s |
| Proteção | Remove possessão, cegueira e náusea; Resistência 30 s e Regeneração 5 s |

Lança, espada flamejante, escudo e armadura têm receitas em
`src/main/resources/data/djinns/recipe`, além de ofertas e acesso criativo.
O rei deixa sua espada ao morrer. Ela incendeia por 5 s; abrir fendas é uma
habilidade exclusiva do chefe, não da arma nas mãos do jogador.
