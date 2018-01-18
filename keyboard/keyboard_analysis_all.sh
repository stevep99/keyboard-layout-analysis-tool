#!/bin/sh

CONFIG_ERGO="config/effort_ergonomic_config.dat"
CONFIG_TRAD="config/effort_traditional_config.dat"
CONFIG_MATRIX="config/effort_matrix_config.dat"

FREQ="freq/en_books.freq"

OUTPUT_OPTS="tb"

cd resources

for i in $( ls *.keyb ); do
  echo Analysing keyboard $i
  if [ "$i" = "colemak_dh.keyb" ]; then
    CONFIG=$CONFIG_ERGO
  elif [ "$i" = "beakl.keyb" ] || [ "$i" = "workman.keyb" ]; then
    CONFIG=$CONFIG_MATRIX
  else
    CONFIG=$CONFIG_TRAD
  fi

  java -cp "../build/libs/keyboard-1.0-SNAPSHOT.jar" io.github.colemakmods.keyboard.KeyboardAnalysis -c $CONFIG -f $FREQ -o $OUTPUT_OPTS $i > ../output/analysis_$i.out
done
