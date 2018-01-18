@echo off

set FREQ_FILE="freq/en_books.freq"

pushd resources
for %%f in (*.keyb) do (
  echo Comparing keyboard %%f with Qwerty
  java -cp "../build/libs/keyboard-1.0-SNAPSHOT.jar" io.github.colemakmods.keyboard.KeyboardCompare -f %FREQ_FILE% qwerty.keyb %%f > ../output/compare_%%f.out
)
popd


