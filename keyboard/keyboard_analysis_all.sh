#!/bin/sh

DIR=`pwd`

CONFIG_ERGO="$DIR/resources/config/effort_ergonomic_config.dat"
CONFIG_TRAD="$DIR/resources/config_3rows/effort_traditional_config.dat"
CONFIG_MATRIX="$DIR/resources/config/effort_matrix_config.dat"

FREQ="$DIR/resources/freq/en_books.freq"

OUTPUT_OPTS="tb"

cd resources/layout

for i in $( ls *.keyb ); do
  echo Analysing keyboard $i
  if [ "$i" = "colemak_dh.keyb" ]; then
    CONFIG=$CONFIG_ERGO
  elif [ "$i" = "beakl.keyb" ] || [ "$i" = "workman.keyb" ]; then
    CONFIG=$CONFIG_MATRIX
  else
    CONFIG=$CONFIG_TRAD
  fi

  java -cp "$DIR/build/libs/keyboard-1.0-SNAPSHOT.jar" io.github.colemakmods.keyboard.KeyboardAnalysis -c $CONFIG -f $FREQ -o $OUTPUT_OPTS $i > $DIR/output/analysis_$i.out
done
