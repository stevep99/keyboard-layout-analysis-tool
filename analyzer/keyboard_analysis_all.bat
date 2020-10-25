@echo off

set CONFIG_ERGO="config/effort_ergonomic_config.dat"
set CONFIG_TRAD="config/effort_traditional_config.dat"

set FREQ="freq/en_books.freq"

set OUTPUT_OPTS="t"

pushd resources
for %%f in (*.keyb) do (
  echo Analysing keyboard %%f
  java -cp "../build/libs/keyboard-1.0-SNAPSHOT.jar" io.github.colemakmods.keyboard.KeyboardAnalysis -c %CONFIG_ERGO% -f %FREQ% -o %OUTPUT_OPTS% %%f > ../output/analysis_%%f.out
)
popd


