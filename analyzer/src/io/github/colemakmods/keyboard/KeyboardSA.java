package io.github.colemakmods.keyboard;

import io.github.colemakmods.chars.BigramFreq;
import io.github.colemakmods.chars.CharFreq;
import io.github.colemakmods.chars.FreqAnalysis;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Perform an analysis on a keyboard layout using provided data on character and
 * bigram frequencies,
 * penalties and finger configuration.
 * 
 * Created by steve on 18/10/14.
 */
public class KeyboardSA {

  public static void main(String args[]) {
    if (args.length < 3) {
      exitHelp();
      return;
    }

    Options options = new Options();
    List<CharFreq> charFreqs = null;
    List<BigramFreq> bigramFreqs = null;

    File keyboardFile = new File(args[args.length - 1]);
    KeyboardLayout keyboardLayout = new KeyboardLayout(keyboardFile.getName());

    boolean valid = KeyboardMapping.parse(keyboardLayout, keyboardFile);
    if (!valid)
      return;
    keyboardLayout.dumpLayout(System.out);

    String alphabet = keyboardLayout.getAlphabet();

    for (int i = 0; i < args.length - 1; ++i) {
      if (args[i].equals("-c")) {
        String[] configFiles = args[++i].split(",");
        for (String configFile : configFiles) {
          valid = KeyboardConfig.parse(keyboardLayout, new File(configFile));
          if (!valid)
            return;
        }
        keyboardLayout.dumpConfig(System.out);

      } else if (args[i].equals("-f")) {
        String frequencyFile = args[++i];
        charFreqs = CharFreq.initialize(alphabet, new File(frequencyFile));
        bigramFreqs = BigramFreq.initialize(alphabet, new File(frequencyFile));
        if (charFreqs == null || bigramFreqs == null) {
          return;
        }

      } else if (args[i].equals("-w")) {
        String wordFile = args[++i];

        FreqAnalysis fa = new FreqAnalysis(alphabet, -1);
        fa.analyze(new File(wordFile));

        charFreqs = fa.getCharFreqs();
        bigramFreqs = fa.getBigramFreqs();

      } else if (args[i].equals("-b")) {
        try {
          String[] bigramsListSizes = args[++i].split(",");
          if (bigramsListSizes.length >= 1) {
            options.sfbListSize = Integer.parseInt(bigramsListSizes[0]);
          }
          if (bigramsListSizes.length >= 2) {
            options.nfbListSize = Integer.parseInt(bigramsListSizes[1]);
          }
        } catch (NumberFormatException ex) {
          System.err.println("Invalid argument: bigram list size (-b)");
          return;
        }

      } else if (args[i].startsWith("-o")) {
        options.outputFormat = args[++i];
      } else if (args[i].startsWith("-t")) {
        options.highTemp = Double.parseDouble(args[++i]);
      } else if (args[i].startsWith("-a")) {
        options.alpha = Double.parseDouble(args[++i]);
      }
    }

    List<String> errors = keyboardLayout.validate();
    if (!errors.isEmpty()) {
      for (String error : errors) {
        System.err.println("Error: " + error);
      }
      return;
    }

    if (charFreqs == null || bigramFreqs == null) {
      exitHelp();
      return;
    }

    KeyboardAnalysis ka = new KeyboardAnalysis();
    keyboardLayout = run_sa(keyboardLayout, charFreqs, bigramFreqs, options.highTemp, options.alpha);
    keyboardLayout.dumpLayout(System.out);
    LayoutResults layoutResults = ka.performAnalysis(keyboardLayout, charFreqs, bigramFreqs);

    try {
      // generate full text report
      if (options.outputFormat.indexOf('t') >= 0) {
        KeyboardAnalysisTextReport kr = new KeyboardAnalysisTextReport(options.sfbListSize, options.nfbListSize);
        String output = kr.generate(layoutResults);
        System.out.println(output);
      }
      // generate brief report for html table
      if (options.outputFormat.indexOf('b') >= 0) {
        KeyboardAnalysisBriefHTMLReport kr = new KeyboardAnalysisBriefHTMLReport();
        String output = kr.generate(layoutResults);
        System.out.println(output);
      }
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private static double probability(double delta, double temp) {
    if (delta < 0)
      return 1;
    return Math.exp((-1 * delta) / temp);
  }

  private static KeyboardLayout run_sa(KeyboardLayout keyboardLayout, List<CharFreq> charFreqs,
      List<BigramFreq> bigramFreqs, double temperature, double alpha) {

    double coolingFactor = alpha;
    double costDelta = 0;
    double stateDelta = 0;
    KeyboardLayout current = keyboardLayout.duplicate();
    double currentEffort = 0;
    double bestEffort = 0;
    double neighbourEffort = 0;
    KeyboardAnalysis ka = new KeyboardAnalysis();

    // int n = current.getKeyList().size();
    // Integer[] arr = IntStream.range(0, n).boxed().toArray(Integer[]::new);
    // List<Integer> intList = Arrays.asList(arr);
    // Collections.shuffle(intList);
    // intList.toArray(arr);

    // for (int i = 0; i < arr.length; i++) {
    // Key key = current.getKeyList().get(i);
    // key.setCol(arr[i] % current.getCols());
    // key.setRow(arr[i] / current.getCols());
    // }
    // current.dumpLayout(System.out);
    KeyboardLayout best = current.duplicate();

    LayoutResults layoutResults = ka.performAnalysis(current, charFreqs, bigramFreqs);
    currentEffort = layoutResults.getTotalFingerEffort();
    bestEffort = currentEffort;

    // for (double t = temperature; t > 1; t *= coolingFactor) {
    double t = temperature;
    while (t > 1e-3) {
      KeyboardLayout neighbour = current.duplicate();
      int key1_idx = (int) (neighbour.getKeyList().size() * Math.random());
      Key key1 = neighbour.getKeyList().get(key1_idx);
      if (key1.getRow() == 0 || key1.getCol() > 9)
        continue;
      int key2_idx;
      Key key2;
      // if (Math.random() < 0.5) {
      key2_idx = (int) (neighbour.getKeyList().size() * Math.random());
      key2 = neighbour.getKeyList().get(key2_idx);
      if (key2.getRow() == 0 || key2.getCol() > 9)
        continue;
      // } else {
      // List<Key> keyNeighbours = new ArrayList<Key>();
      // for (int i = -1; i < 2; i++) {
      // Key neighbourKey = neighbour.lookupKey(key1.getRow(), key1.getCol() + i);
      // if (neighbourKey != null) {
      // keyNeighbours.add(neighbourKey);
      // }
      // }
      // for (int i = -1; i < 2; i++) {
      // Key neighbourKey = neighbour.lookupKey(key1.getRow() + i, key1.getCol());
      // if (neighbourKey != null) {
      // keyNeighbours.add(neighbourKey);
      // }
      // }
      // Random rand = new Random();
      // key2 = keyNeighbours.get(rand.nextInt(keyNeighbours.size()));
      // }

      Key key1_copy = new Key(key1.getRow(), key1.getCol(), key1.getChars());

      key1.setChars(key2.getChars());

      key2.setChars(key1_copy.getChars());

      layoutResults = ka.performAnalysis(neighbour, charFreqs, bigramFreqs);
      neighbourEffort = layoutResults.getTotalFingerEffort();
      double delta = coolingFactor * (neighbourEffort - currentEffort);
      boolean accepted = Math.random() < probability(delta, t);
      // System.out.println("Temp " + t + " costDelta " + costDelta + " stateDelta " +
      // stateDelta + " delta " + delta + " accepted " + accepted);
      if (delta > 0) {
        stateDelta -= (delta / t);
      }
      if (accepted) {
        costDelta += delta;
        current = neighbour.duplicate();
        currentEffort = neighbourEffort;
      }
      t = calc_temp(costDelta, stateDelta, temperature, coolingFactor);

      if (currentEffort < bestEffort) {
        // System.out.println(
        // "Best layout updated currentEffort " + currentEffort + " bestEffort " +
        // bestEffort + " temperature " + t);
        // System.out.println("Temp " + t + " costDelta " + costDelta + " stateDelta " +
        // stateDelta + " delta " + delta);
        best = current.duplicate();
        bestEffort = currentEffort;
        // best.dumpLayout(System.out);
      }

    }

    System.out.println("Temp " + t + " costDelta " + costDelta + " stateDelta " + stateDelta);
    return best;

  }

  private static double calc_temp(double costDelta, double stateDelta, double highTemp, double alpha) {
    if (costDelta >= 0 | stateDelta == 0) {
      return highTemp;
    }
    double newTemp = alpha * costDelta / stateDelta;
    if (newTemp < highTemp) {
      return newTemp;
    }
    return highTemp;
  }

  private static void exitHelp() {
    System.out.println("KeyboardAnalysis  -c configFiles..  -f frequencyFile  keyboardFile");
    System.out.println("KeyboardAnalysis  -c configFiles..  -w wordFile  keyboardFile");
    System.exit(0);
  }

  public LayoutResults performAnalysis(KeyboardLayout keyboardLayout, List<CharFreq> charFreqs,
      List<BigramFreq> bigramFreqs) {
    List<String> messages = sanityCheck(keyboardLayout);

    HashMap<Key, Double> keyFreq = calculateKeyFrequency(keyboardLayout, charFreqs);
    double[] fingerFreq = calculateFingerFrequency(keyboardLayout, charFreqs);

    double handAlternation = countHandAlternation(keyboardLayout, bigramFreqs);

    // find difficult bigrams for the current layout
    List<FingerBigram> sameFingerBigrams = findSameFingerBigrams(keyboardLayout, bigramFreqs);
    List<FingerBigram> neighbourPenaltyFingerBigrams = findNeighbourFingerBigrams(keyboardLayout, bigramFreqs, false);
    List<FingerBigram> neighbourBenefitFingerBigrams = findNeighbourFingerBigrams(keyboardLayout, bigramFreqs, true);

    double[][] fingerEffort = new double[3][];
    fingerEffort[0] = calculateBaseEffort(keyboardLayout, charFreqs);
    fingerEffort[1] = calculateSameFingerBigramEffort(keyboardLayout, sameFingerBigrams);
    fingerEffort[2] = calculateNeighbourFingerBigramEffort(keyboardLayout,
        joinList(neighbourPenaltyFingerBigrams, neighbourBenefitFingerBigrams));

    return new LayoutResults(keyboardLayout, messages, keyFreq, fingerFreq, handAlternation,
        sameFingerBigrams, neighbourPenaltyFingerBigrams, neighbourBenefitFingerBigrams, fingerEffort);
  }

  private List<FingerBigram> joinList(List<FingerBigram> list1, List<FingerBigram> list2) {
    List<FingerBigram> list = new ArrayList<>();
    list.addAll(list1);
    list.addAll(list2);
    return list;
  }

  private List<String> sanityCheck(KeyboardLayout keyboardLayout) {
    List<String> messages = new ArrayList<>();

    String alphabet = keyboardLayout.getAlphabet();
    if (keyboardLayout.getRows() > 4) {
      messages.add("Warning: Too many rows");
    }

    for (char c = 'A'; c <= 'Z'; ++c) {
      if (alphabet.indexOf(c) < 0) {
        messages.add("Warning: letter " + c + " is missing from layout. ");
      }
    }
    for (int i = 0; i < alphabet.length(); ++i) {
      char c = alphabet.charAt(i);
      if (alphabet.indexOf(c, i + 1) >= 0) {
        messages.add("Warning: letter " + c + " appears more than once on layout. ");
      }
    }
    return messages;
  }

  private HashMap<Key, Double> calculateKeyFrequency(KeyboardLayout keyboardLayout, List<CharFreq> charFreqs) {
    HashMap<Key, Double> keyFrequency = new HashMap<>();
    for (int row = 0; row < keyboardLayout.getRows(); ++row) {
      for (int col = 0; col < keyboardLayout.getCols(); ++col) {
        Key key = keyboardLayout.lookupKey(row, col);
        if (key != null) {
          Double freqTotal = 0.;
          for (char ch : key.getChars().toCharArray()) {
            CharFreq cf = CharFreq.findByChar(ch, charFreqs);
            if (cf != null)
              freqTotal += cf.getFreq();
          }
          keyFrequency.put(key, freqTotal);
        }
      }
    }
    return keyFrequency;
  }

  private double[] calculateFingerFrequency(KeyboardLayout keyboardLayout, List<CharFreq> charFreqs) {
    double[] fingerFreq = new double[10];

    for (CharFreq charFreq : charFreqs) {
      char c = charFreq.getChar();
      Key key = keyboardLayout.lookupKey(c);
      if (key != null) {
        fingerFreq[key.getFinger()] += charFreq.getFreq();
      }
    }

    return fingerFreq;
  }

  private double countHandAlternation(KeyboardLayout keyboardLayout, List<BigramFreq> bigramFreqs) {
    double handAlternation = 0f;
    for (BigramFreq bigramFreq : bigramFreqs) {
      char[] chars = bigramFreq.getString().toCharArray();
      Key key1 = keyboardLayout.lookupKey(chars[0]);
      Key key2 = keyboardLayout.lookupKey(chars[1]);
      if (key1.getHand() != key2.getHand()) {
        handAlternation += bigramFreq.getFreq();
      }
    }
    return handAlternation;
  }

  /**
   * Return a list of all same-finger bigrams for this layout
   *
   * @param keyboardLayout the current keyboard layout
   * @param bigramFreqs    the full set of bigram frequencies
   * @return the list of same-finger bigrams found
   */
  private List<FingerBigram> findSameFingerBigrams(KeyboardLayout keyboardLayout, List<BigramFreq> bigramFreqs) {
    List<FingerBigram> fingerBigrams = new ArrayList<FingerBigram>();

    for (BigramFreq bigramFreq : bigramFreqs) {
      char[] chars = bigramFreq.getString().toCharArray();
      Key key1 = keyboardLayout.lookupKey(chars[0]);
      Key key2 = keyboardLayout.lookupKey(chars[1]);
      if (key1 != null && key2 != null) {
        if (chars[0] != chars[1] && KeyboardConfig.isSameFinger(key1.getFinger(), key2.getFinger())) {
          FingerBigram fingerBigram = new FingerBigram(key1, key2, bigramFreq);
          fingerBigrams.add(fingerBigram);
        }

      }
    }

    return fingerBigrams;
  }

  /**
   * Return a list of neighbour-finger bigrams for this layout that have defined
   * penalties
   *
   * @param keyboardLayout the current keyboard layout
   * @param bigramFreqs    the full set of bigram frequencies
   * @param benefit        whether to get beneficial or detrimental bigrams
   * @return the list of neighbour-finger bigrams found
   */
  private List<FingerBigram> findNeighbourFingerBigrams(KeyboardLayout keyboardLayout, List<BigramFreq> bigramFreqs,
      boolean benefit) {
    List<FingerBigram> fingerBigrams = new ArrayList<FingerBigram>();

    for (BigramFreq bigramFreq : bigramFreqs) {
      char[] chars = bigramFreq.getString().toCharArray();
      Key key1 = keyboardLayout.lookupKey(chars[0]);
      Key key2 = keyboardLayout.lookupKey(chars[1]);
      if (key1 != null && key2 != null) {
        if (KeyboardConfig.isNeighbourFinger(key1.getFinger(), key2.getFinger())) {
          int outermostFinger = KeyboardConfig.getOutermostFinger(key1.getFinger(), key2.getFinger());
          FingerBigram fingerBigram = new FingerBigram(key1, key2, bigramFreq);
          if (keyboardLayout.hasPenaltyNeighbourFinger(outermostFinger) != benefit) {
            fingerBigrams.add(fingerBigram);
          }
        }

      }
    }

    return fingerBigrams;
  }

  /**
   * Calculate the base effort incurred due to key position
   */
  private double[] calculateBaseEffort(KeyboardLayout keyboardLayout, List<CharFreq> charFreqs) {
    double[] baseEffort = new double[10];
    for (char ch : keyboardLayout.getAlphabet().toCharArray()) {
      Key key = keyboardLayout.lookupKey(ch);
      CharFreq cf = CharFreq.findByChar(ch, charFreqs);
      if (cf != null) {
        baseEffort[key.getFinger()] += key.getEffort() * cf.getFreq();
      }
    }
    return baseEffort;
  }

  /**
   * Calculate the effort incurred due to same-finger bigram penalties
   */
  private double[] calculateSameFingerBigramEffort(KeyboardLayout keyboardLayout, List<FingerBigram> fingerBigrams) {
    double[] bigramEffort = new double[10];
    for (FingerBigram fingerBigram : fingerBigrams) {
      int finger1 = fingerBigram.getKey1().getFinger();
      int diffrows = Math.min(2, Math.abs(fingerBigram.getKey1().getRow() - fingerBigram.getKey2().getRow()));
      double penalty = keyboardLayout.getPenaltySameFinger(diffrows);
      bigramEffort[finger1] += fingerBigram.getBigramFreq().getFreq() * penalty;
    }
    return bigramEffort;
  }

  /**
   * Calculate the effort incurred due to neighbour-finger bigram penalties
   */
  private double[] calculateNeighbourFingerBigramEffort(KeyboardLayout keyboardLayout,
      List<FingerBigram> fingerBigrams) {
    double[] bigramEffort = new double[10];
    for (FingerBigram fingerBigram : fingerBigrams) {
      int finger1 = fingerBigram.getKey1().getFinger();
      int finger2 = fingerBigram.getKey2().getFinger();
      int outermostFinger = KeyboardConfig.getOutermostFinger(finger1, finger2);
      int diffrows = Math.min(2, Math.abs(fingerBigram.getKey1().getRow() - fingerBigram.getKey2().getRow()));
      double penalty = keyboardLayout.getPenaltyNeighbourFinger(outermostFinger, diffrows);
      bigramEffort[outermostFinger] += fingerBigram.getBigramFreq().getFreq() * penalty;
    }
    return bigramEffort;
  }

}
