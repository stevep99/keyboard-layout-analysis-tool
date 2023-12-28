
# Keyboard Layout Analysis Tool

This project provides tools for analyzing the efficiency and ease-of-use of alternative keyboard layouts. The analysis is done according to a set of criteria including (i) the relationship between the position of each key and the frequency with which it is typed, and (ii) the frequency of awkward combinations of keys, most notably same-finger bigrams.

A well-designed keyboard layout should have the most frequently typed keys in easy-to-reach positions, and should avoid frequent awkward hand and/or finger stretches. To allow smooth and comfortable typing, it should also avoid situations where frequent combinations of keys are made unduly difficult to type. At the risk of stating the obvious, the standard "Qwerty" keyboard performs badly in these kinds of tests. However, by following these principles of sound ergonomic design, it possible to create keyboard layouts which require less finger strain and are more comfortable to use. This program allows alternative layouts to be compared and studied.

The model used is defined in more detail at http://colemakmods.github.io/mod-dh/compare.html, where you can also see the results produced for a variety of layouts. Using this code you can reproduce those results, or, by adjusting the parameters of the model, generate your own results. You can also try creating and testing out your own arbitrary layouts.

If you want to use this code to analyze layouts but would rather avoid compiling the code, then use this online version of the app: http://colemakmods.github.io/mod-dh/analyze.html

If you want to know more about the letter frequency distrubution statistics used by this analyzer, take a look at this page describing the [frequency data](data/README.md).

The project contains two modules: "keyboard" and "keyboard-web"

## The "keyboard" Module

This module is the main Java app. You can use this to:

(i) analyze various keyboard layouts and generate typing efficiency results

(ii) compare the ease-of-learning (switching difficulty) of various layouts, using [this scheme ](http://colemakmods.github.io/mod-dh/learn.html)

(iii) generate a letter and bigram frequencies for a given input text (corpus)


### Compiling

The app is built using [gradle](https://gradle.org/).

1. Ensure you have a recent JDK (recommended version 8+) installed.
2. ```cd keyboard```
3. ```../gradlew jar```
4. If compiling was successful, you should have generated a file ```keyboard/build/libs/analyzer.jar```.

### Running

If compiling was successful, you should be run the provided scripts:

```keyboard_analysis``` is the main script for analyzing layouts. Run without arguments for Usage details.

```keyboard_analysis_all``` will run the analysis on all the keyboard layouts defined in the ```resources``` directory. The results will be written to the ```output``` directory.

```keyboard_compare``` will compare a layout (i.e. changes) against a base layout, usually Qwerty, in order to estimate the learning difficulty. Run without arguments for Usage details.

```keyboard_compare_all``` will run the comparison tool on all the keyboard layouts defined in the ```resources``` directory. The results will be written to the ```output``` directory.

```freq_analysis``` will generate the character and bigram frequencies for an input text corpus. This can in turn be used by the keyboard_analysis tool if you want results based on an alternative corpus, e.g. for a foreign language.

### Resources

The ```resources``` folder contains all the input files which are used by the analysis model:

- A list of alternative keyboard layouts in ```resources/layout/*.keyb```.

- Letter and bigram frequency tables in ```resources/freq/*```.

- Keyboard configuration items, defining penalties and finger assignments in ```resources/config/*```. See [here](http://colemakmods.github.io/mod-dh/analyze.html) for detailed explanation.

## The "keyboard-web" Module

You probably can ignore this module, it is only for generating the layout analyzer's [web interface](http://colemakmods.github.io/mod-dh/analyze.html).

### Compiling

The module is build using [gradle](https://gradle.org/), and uses [TeaVM](http://teavm.org/) to generate the JavaScript parts.

1. Ensure you have a recent JDK (recommended version 8+) installed.
2. Ensure you have maven installed.
3. ```cd keyboard-web```
4. copy the resource files ```./prepare-resources.sh```
4. ```../gradlew webapp```
5. If compiling was successful, you should have the generated web files in ```keyboard-web/build/webapp```


