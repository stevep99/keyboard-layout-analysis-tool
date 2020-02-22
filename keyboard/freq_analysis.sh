#!/bin/sh 

java -cp "build/libs/keyboard-1.0-SNAPSHOT.jar" io.github.colemakmods.chars.FreqAnalysis $*

#example command:

BOOKS="$HOME/apps/carpalx-0.11/corpus/books.txt"
ALPHABET="ABCDEFGHIJKLMNOPQRSTUVWXYZ,.;:'\"/?-"

#java -cp "build/libs/keyboard-1.0-SNAPSHOT.jar" io.github.colemakmods.chars.FreqAnalysis -a $ALPHABET $BOOKS > output/en_books.freq


