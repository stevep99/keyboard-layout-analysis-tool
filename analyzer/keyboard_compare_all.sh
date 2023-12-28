#!/bin/sh

DIR=`pwd`

CONFIG_ERGO="$DIR/resources/config/effort_ergonomic_config.dat"
CONFIG_ALT="$DIR/resources/config/effort_alternative_config.dat"
CONFIG_TRAD="$DIR/resources/config/effort_traditional_config.dat"
CONFIG_MATRIX="$DIR/resources/config/effort_matrix_config.dat"

FREQ_ARG="-f $DIR/resources/freq/en_books.freq"

cd resources/layout_full

for i in $( ls *.keyb ); do

  if [ "$i" = "colemak_dh.keyb" ] || [ "$i" = "colemak_dhk.keyb" ] ; then
    POS_OVERRIDE_ARG="-p $DIR/resources/config/pos_override_colemak_dh.dat"
    CONFIG=$CONFIG_ERGO
  else
    POS_OVERRIDE_ARG="" 
    CONFIG=$CONFIG_TRAD
  fi
  
  echo "Comparing keyboard $i with Qwerty"

  java -cp "$DIR/build/libs/analyzer.jar" io.github.colemakmods.keyboard.KeyboardCompare \
	  $FREQ_ARG $POS_OVERRIDE_ARG \
	  qwerty.keyb $CONFIG_TRAD \
	  $i $CONFIG > $DIR/output/compare_$i.out
done
