#!/bin/sh

DIR=`pwd`

FREQ_FILE="$DIR/resources/freq/en_books.freq"

cd resources/layout_main
for i in $( ls *.keyb ); do
  echo Comparing keyboard $i with Qwerty

  if [ "$i" = "colemak_dh.keyb" ]; then
    POS_OVERRIDE_ARG="-p $DIR/resources/config/pos_override_colemak_dh.dat"
  else
    POS_OVERRIDE_ARG=""
  fi

  java -cp "$DIR/build/libs/keyboard-1.0-SNAPSHOT.jar" io.github.colemakmods.keyboard.KeyboardCompare -f $FREQ_FILE $POS_OVERRIDE_ARG qwerty.keyb $i > $DIR/output/compare_$i.out
done
