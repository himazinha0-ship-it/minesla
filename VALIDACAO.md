# Validação da versão 1.1.0

O artefato é uma atualização incremental do JAR 1.0.0 corrigido fornecido pelo
usuário. Ele inclui a vila no deserto, o rei maior e detalhado, a espada voxel
animada e as novas curvas de movimento. A declaração do refmap corrigida
anteriormente foi preservada.

## Verificações concluídas

- Bytecode: análise de fluxo/pilha das **14 classes e 112 métodos** do JAR.
- Ciclo de golpe: o método de produção do rei foi carregado e executado pela
  JVM em uma fixture estreita de superclass, com verificação de frames. O
  ciclo dura **18 ticks** e djinns comuns continuam delegando ao ciclo do pai.
- Curvas: **9.608 amostras** verificadas, com valores finitos, continuidade nos
  keyframes de ataque e recuperação para a pose de repouso; dano básico 10.
- Modelo: o bytecode real do modelo foi executado com substitutos estreitos
  da API Minecraft de modelos, em **1.448 poses**. Foram conferidos hierarquia,
  **97 cuboides**, antebraços, coroa exclusiva do rei e transformação da espada.
- Estrutura: o NBT foi decodificado por um leitor independente do escritor.
  Conferidos **60.512 blocos**, limites e índices, os **29 moradores**, todas as
  16 variantes, exatamente um rei e ausência de blocos nos volumes iniciais
  das criaturas. Vínculos de biome, structure, structure set, pool e loot válidos.
- Recursos: **75 JSON, 34 PNG** com CRC/zlib íntegros, modelos/texturas, receitas,
  traduções PT-BR/inglês e refmap do JAR instalável.
- Arte: inspeção de prévias extraídas dos dados de geometria efetivamente
  inseridos no modelo, do modelo voxel da espada e da planta da vila.
- Fontes: parser Java 21 nos 12 arquivos, sem erros sintáticos. As duas novas
  classes independentes do Minecraft foram compiladas com JDK 21.

## Limites explícitos

Não foi iniciado um cliente ou servidor Minecraft completo. Os substitutos de
API e verificações de bytecode não substituem execução real de geração,
renderização, navegação, combate e sincronização multiplayer. Esses testes
permanecem necessários. A estrutura foi conferida como dados; sua colocação
real e desempenho na geração ainda precisam ser observados no Minecraft.

A compilação integral Gradle continua sem acesso às dependências Fabric. O JAR
foi produzido por `tools/update/BuildUpdate.java`, que mantém a base remapeada,
atualiza cinco classes existentes e insere duas classes auxiliares, sem embutir
a ferramenta de build no produto. O manifesto registra o método incremental.

O pacote distribuído contém somente o mod, instruções e verificações de hash.
Não inclui corretor, instalador, scripts executáveis ou os substitutos da API.
Não houve análise por Windows Defender nem confirmação sobre eventuais alertas.

## Teste no jogo ainda necessário

1. Substituir o mod antigo pela versão 1.1.0, usando Fabric/Minecraft 1.21.1.
2. Entrar em um mundo e confirmar que não há regressão do erro de mixin.
3. Localizar a vila em chunks novos de deserto com
   `/locate structure djinns:djin_city_capital` e conferir terrenos, baús,
   moradores, palácio e rei. Para um teste manual em área aberta, usar
   `/place structure djinns:djin_city_capital ~ ~ ~`.
4. Conferir o tamanho de cinco blocos do rei, poses de repouso, golpe,
   conjuração, recuperação e espada acompanhando a mão, em diferentes FPS.
5. Conferir o atributo de ataque total 10 sem encantamentos e antes de armadura,
   o golpe em arco, fogo e fendas; repetir em um rei salvo de versão anterior.
6. Testar negociação, pergaminhos, possessão e retorno do movimento após cinco
   segundos, além de persistência após salvar/reabrir.
7. Repetir em servidor dedicado com o mesmo JAR nos dois lados.
