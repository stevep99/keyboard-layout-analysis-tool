#!/bin/sh

FREQ_FILE="freq/en_books.freq"

cd resources
for i in $( ls *.keyb ); do
  echo Comparing keyboard $i with Qwerty

  if [ "$i" = "colemak_dh.keyb" ]; then
    POS_OVERRIDE_ARG="-p config/pos_override_colemak_dh.dat"
  else
    POS_OVERRIDE_ARG=""
  fi

  java -cp "../build/libs/keyboard-1.0-SNAPSHOT.jar" io.github.colemakmods.keyboard.KeyboardCompare -f $FREQ_FILE $POS_OVERRIDE_ARG qwerty.keyb $i > ../output/compare_$i.out
done
