# Djinns: Espíritos do Deserto — 1.1.0

Mod para **Minecraft Java 1.21.1**, **Fabric Loader 0.16.14+**, **Fabric API para
1.21.1** e **Java 21**. Instale a mesma versão do mod no cliente e no servidor.

[Baixar o JAR 1.1.0](dist/djinns-fabric-1.21.1-1.1.0.jar) ·
[Baixar o ZIP contendo o mod](dist/djinns-fabric-1.21.1-1.1.0.zip)

## Atualização 1.1.0

- **Vila dos djinns no deserto:** estrutura `djinns:djin_city_capital`, com
  97 × 97 blocos de área, muralhas, entrada com estátuas, mercado, casas,
  canais, pontes, fontes, palmeiras, minaretes e um palácio com cúpula turquesa.
  A composição é inspirada na referência de cidade, adaptada à escala de uma
  vila de Minecraft. São 29 moradores, incluindo todas as 16 variantes e um rei.
- **Rei maior:** escala visual 2,05, altura de colisão de cinco blocos e largura
  de 1,4. O modelo ganhou coroa, joias, lamelas nas ombreiras, corrente,
  manoplas, tabardo e capa. A malha completa tem 97 cuboides, contando as peças
  compartilhadas e as exclusivas. A textura do rei foi refeita.
- **Espada real tridimensional:** 80 elementos com núcleo escuro, lâmina e
  chamas ciano, guarda dourada com detalhes em marfim e cabo escuro. O atlas
  de textura tem seis quadros com interpolação. O desenho é inspirado na
  espada de referência; não é uma cópia pixel a pixel da imagem.
- **Dano básico total da espada: 10**, antes de armadura e encantamentos.
  O modificador de +9 soma ao ataque básico de 1. O rei também usa base 1,
  incluindo migração desse atributo quando um rei salvo é carregado. O fogo
  da espada permanece uma fonte adicional de dano ao longo do tempo.
- **Animações:** o ciclo de golpe do rei dura 18 ticks (0,9 s a 20 TPS), com
  curvas quinticas de preparação, corte e recuperação. Braços e antebraços
  articulam, e a espada acompanha a mão. Corpo, cauda de quatro segmentos e
  capa usam ondas contínuas. A conjuração entra e sai gradualmente.
- O golpe em arco do rei causa 10 de dano básico, tem alcance frontal de até
  4,4 blocos e intervalo de 28 ticks. Fendas e guardas invocados permanecem.

## Instalar

1. Feche o Minecraft.
2. Mova a cópia antiga de Djinns para uma pasta de backup fora de `mods`.
3. Coloque **`djinns-fabric-1.21.1-1.1.0.jar`** em `mods`. Deixe apenas uma cópia
   de Djinns e mantenha o Fabric API para Minecraft 1.21.1 instalado.
4. Abra o perfil Fabric de Minecraft **1.21.1**.

O ZIP contém o JAR, instruções e checksum. Extraia o ZIP e coloque o **JAR**
em `mods`. Não há corretor, instalador, arquivo BAT ou ferramenta adicional.

## Encontrar a vila

A geração natural acontece somente no **deserto**, em terreno ainda não gerado.
Uma atualização não constrói a vila retroativamente em chunks existentes.
Ela ocupa 97 × 97 blocos e contém um rei persistente no palácio.

Com cheats ou permissão de operador:

```mcfunction
/locate structure djinns:djin_city_capital
```

Para colocação manual, em uma área aberta do deserto:

```mcfunction
/place structure djinns:djin_city_capital ~ ~ ~
```

A vila é uma estrutura vanilla jigsaw de uma peça, sem geração customizada de
blocos por código do servidor. Suas casas têm postos de trabalho e baús com
lápis-lazúli, ouro, comida e chance de pergaminhos.

## Funcionalidades mantidas

- Quatro cores (azul, vermelho, branco, negro) × quatro papéis (comum,
  comerciante, guarda, mago), além do rei. Os ovos estão na aba criativa Djinns.
- Djinns neutros e flutuantes; dano chama aliados em até 32 blocos por 30 s.
- Vermelhos usam fogo e projéteis de blaze. Brancos aplicam Levitação.
  Negros aplicam Lentidão II e Escuridão. Magos têm ataques à distância.
- Golpes corpo a corpo de jogador têm 15% de chance de causar Possessão,
  Cegueira e Náusea por cinco segundos. O movimento é involuntário e o jogador
  ataca criaturas próximas; esses ataques forçados não atingem jogadores.
- Comuns, comerciantes e magos negociam por lápis-lazúli na interface vanilla.
  Há 13 profissões, níveis de comércio, reposição diária e quatro pergaminhos.
- Guardas usam lança/espada, escudo e armadura. O rei mantém barra de chefe,
  300 de vida, invocação por fendas e maior frequência de invocação a meia vida.
- A correção de entrada no mundo é preservada: o mixin de possessão declara
  o refmap que resolve o método de movimento na instalação de Minecraft.

Comandos de teste existentes:

```mcfunction
/function djinns:kit
/function djinns:showcase
/summon djinns:djinn_king ~ ~1 ~
```

## Validação e limites

O JAR 1.1.0 foi construído sobre o JAR corrigido fornecido pelo usuário.
Novas classes auxiliares foram compiladas com JDK 21. Uma etapa interna de
build aplica as mudanças de geometria, atributos e animação ao bytecode já
remapeado; essa ferramenta interna não faz parte do mod distribuído.

Passaram: análise de bytecode das 14 classes/112 métodos; execução JVM do
método de golpe; 9.608 amostras de continuidade de animação; 1.448 poses do
modelo real executadas com substitutos estreitos da API de modelos; leitura
independente do NBT da vila, conferência dos 29 moradores sem interseção com
blocos, vínculos de worldgen/loot; recursos e refmap do JAR final.

**Não foi executado um cliente ou servidor Minecraft completo neste ambiente.**
A geração no jogo, negociação, combate e sincronização multiplayer ainda
precisam de teste dentro do Minecraft. As prévias exportadas em `build/previews`
são renderizações dos modelos, não capturas do jogo.

## Fontes e reprodução

O projeto usa Java 21 e Fabric Loom 1.7.4. Em uma máquina com acesso às
bibliotecas Fabric/Mojang, o fluxo de desenvolvimento é `./gradlew build`,
`./gradlew runClient` e `./gradlew runServer`. O build confere o refmap de
produção para impedir a regressão do erro de entrada no mundo.

Os passos usados para o JAR incremental desta entrega estão em `tools/update`:
`generate_update.py`, `BuildUpdate.java`, `VerifyBytecode.java`, `PoseChecks.java`,
`test_model_runtime.py` e `check_capital.py`. Arte e NBT são gerados localmente;
Pillow e NumPy são ferramentas de desenvolvimento e não dependências do mod.
A compilação integral por Gradle permanece bloqueada neste ambiente pelo
acesso às dependências Fabric. O JAR incremental não depende desse download.
