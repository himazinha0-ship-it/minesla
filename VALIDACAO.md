# Validação da versão 1.1.1

O JAR atualiza a versão 1.1.0, preservando sua base remapeada e o refmap
que corrigiu o erro de entrada no mundo. Não inclui instalador ou corretor.

## Verificações executadas

- 21 classes e 163 métodos: análise ASM de pilha e fluxo.
- 292 verificações de comportamento: despacho pelo Brigadier real, prioridade
  do literal da capital sobre o argumento vanilla, consultas assíncronas,
  futuros incompletos, cancelamento, prazo, limite de 128 candidatos e quatro
  buscas simultâneas, limpeza ao encerrar servidor e coordenadas de um início
  de estrutura efetivamente retornado. O mundo é um substituto de teste.
- 44 referências Minecraft das novas classes: descritores conferidos com
  Yarn 1.21.1+build.3 ou com referências já presentes no JAR anterior.
- Métodos reais do JAR carregados e executados pela JVM com substitutos de
  superclass: identificação do invocador, alvo, duração de 1.200 ticks, zero
  XP, contador de conjuração 60, delegação de navegação terrestre e golpe de
  18 ticks. Esses testes não executam a IA real do Minecraft.
- Invocações com espaço parcialmente bloqueado: três guardas na primeira
  onda, quatro cores ao longo das ondas, alvo herdado, limite de seis ativos,
  ausência de invocações sem alvo e tentativa limitada quando tudo está bloqueado.
- Modelo da espada: matrizes vanilla de item na mão aplicadas às coordenadas
  do cabo e da lâmina do recurso final. Erro do centro da empunhadura abaixo
  de 0,01 pixel do modelo nas duas mãos; lâmina alinhada para cima.
- NBT independente: 60.512 blocos, 29 moradores sem interseção inicial com
  blocos, todas as variantes, um rei, gravidade habilitada e vínculos de geração.
- 75 JSON, 34 PNG, 17 entidades, 12 equipamentos/pergaminhos, sete receitas,
  traduções e refmap no JAR final. Nenhum substituto da API foi incluído no
  mod. Parser sintático Java 21 nos 15 arquivos de desenvolvimento.

## Limites

Não foi possível iniciar um cliente ou servidor Minecraft completo: o acesso
às dependências Fabric/Mojang permanece bloqueado neste ambiente. A versão
usa o build incremental reproduzível em `tools/fixes/BuildFixes.java`.
Os testes acima não comprovam desempenho, renderização ou navegação no jogo.
Não houve varredura por Windows Defender.

## Conferência necessária no jogo

1. Instalar somente o JAR 1.1.1, Fabric API para 1.21.1, Java 21 e Loader 0.16.14+.
2. Entrar em um mundo existente e em um novo; executar
   `/locate structure djinns:djin_city_capital`, observar o movimento continuar
   durante a busca, cancelar com `/djinns cancelar` e tentar em um deserto.
3. Conferir a espada nas poses de repouso, preparação, corte e recuperação.
4. Em Sobrevivência, atacar o rei e esperar cerca de três segundos a 20 TPS:
   conferir fendas, guardas armados e perseguição. Em Criativo, o rei ignora o
   jogador como alvo; esse comportamento já existia no mod.
5. Conferir a navegação junto ao chão de djinns novos, invocados e já salvos.
6. Repetir com o mesmo JAR no cliente e servidor dedicado.

A busca confirma candidatos dentro de um raio limitado e não certifica a
capital mais próxima de todo o mundo. Sem resultado, tentar perto de um deserto.
