#!/bin/sh

DIR=`pwd`

CONFIG_ERGO="$DIR/resources/config/effort_ergonomic_config.dat"
CONFIG_ALT="$DIR/resources/config/effort_alternative_config.dat"
CONFIG_TRAD="$DIR/resources/config/effort_traditional_config.dat"
CONFIG_MATRIX="$DIR/resources/config/effort_matrix_config.dat"

FREQ="$DIR/resources/freq/en_books.freq"

OUTPUT_OPTS="tb"

cd resources/layout_main

for i in $( ls *.keyb ); do
  echo Analysing keyboard $i
  if [ "$i" = "colemak_dh.keyb" ] || [ "$i" = "colemak_dhk.keyb" ] || [ "$i" = "colemak2.keyb" ]; then
    CONFIG=$CONFIG_ERGO
  elif [ "$i" = "colemak_dh2.keyb" ] || [ "$i" = "workman.keyb" ]; then
    CONFIG=$CONFIG_MATRIX
  elif [ "$i" = "niro.keyb" ] || [ "$i" = "soul.keyb" ]; then
    CONFIG=$CONFIG_ALT
  else
    CONFIG=$CONFIG_TRAD
  fi

  java -cp "$DIR/build/libs/analyzer-1.0-SNAPSHOT.jar" io.github.colemakmods.keyboard.KeyboardAnalysis -c $CONFIG -f $FREQ -b 200,5 -o $OUTPUT_OPTS $i > $DIR/output/analysis_$i.out
done
