#!/bin/sh

java -cp "build/libs/keyboard-1.0-SNAPSHOT.jar" io.github.colemakmods.chars.WordAnalysis $*

TEXT="$HOME/apps/carpalx-0.11/corpus/books/prideprej.txt"

#java -cp "build/libs/keyboard-1.0-SNAPSHOT.jar" io.github.colemakmods.chars.WordAnalysis -f $TEXT > output/prideprej_words.txt

