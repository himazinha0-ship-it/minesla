# Correções incrementais 1.1.1

Entrada: JAR 1.1.0 remapeado em `dist`. Saída: somente o mod 1.1.1.
Não embutir as fixtures de API, Brigadier, ASM ou esta ferramenta no produto.
O projeto também contém fontes nomeadas normais para o build completo Loom.

Reprodução neste workspace (JDK 21 e Gradle 8.8 em `/workspace/tools`):

```sh
python tools/update/generate_update.py
python tools/fixes/compile_runtime.py
/workspace/tools/jdk-21.0.9+10/bin/javac -proc:none -cp '/workspace/tools/gradle-8.8/lib/*' -d build/fixes tools/fixes/BuildFixes.java
/workspace/tools/jdk-21.0.9+10/bin/java -cp 'build/fixes:/workspace/tools/gradle-8.8/lib/*' BuildFixes dist/djinns-fabric-1.21.1-1.1.0.jar build/fix-api/classes src/main/resources dist/djinns-fabric-1.21.1-1.1.1.jar
```

`compile_runtime.py` precisa das classes Brigadier em `build/brigadier-api`.
São compiladas do fonte oficial Mojang/brigadier; servem somente ao build/teste.
As classes Minecraft/Fabric geradas pelo script são substitutos estreitos;
não representam execução do jogo ou compilação integral contra Minecraft.
`BuildFixes` copia exclusivamente as classes `dev/minesla/djinn/runtime`.

Testes de comportamento e empunhadura:

```sh
/workspace/tools/jdk-21.0.9+10/bin/javac -proc:none -cp 'build/fix-api/classes:build/brigadier-api' -d build/fix-api/classes tools/fixes/RuntimeChecks.java
/workspace/tools/jdk-21.0.9+10/bin/java -Xverify:all -cp 'build/fix-api/classes:build/brigadier-api' RuntimeChecks
python tools/fixes/check_grip.py dist/djinns-fabric-1.21.1-1.1.1.jar
python tools/validate_resources.py --jar dist/djinns-fabric-1.21.1-1.1.1.jar
python tools/update/check_capital.py
```

`VerifyFixMethods.java` usa ASM do Gradle e os mappings oficiais Yarn
1.21.1+build.3 para conferir os descritores novos e executar os métodos reais
de ponte na JVM. Argumentos: JAR final, JAR 1.1.0, diretório mappings do Yarn
e `build/fix-api/classes`. `VerifyBytecode.java`, em `tools/update`, também
exige `asm-analysis` 9.7 do Maven Central.

Após alterar `runtime/*.java`, atualize as fontes normais com
`python tools/fixes/named_sources.py /caminho/yarn/mappings`.

A busca mantém spacing=36, separation=12 e salt=73110429 para compatibilidade
com a geração 1.1.0. O executor auxiliar apenas solicita o futuro público do
chunk, que o Minecraft encaminha ao seu executor. O tick consulta futuros já
concluídos. Invocar esse método de chunk diretamente no tick faria o
Minecraft esperar sincronamente. Toda inspeção de estruturas permanece no tick.
