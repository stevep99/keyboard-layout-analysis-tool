package io.github.colemakmods.keyboard;

import io.github.colemakmods.chars.BigramFreq;
import io.github.colemakmods.chars.CharFreq;
import io.github.colemakmods.chars.FreqAnalysis;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Perform an analysis on a keyboard layout using provided data on character and bigram frequencies,
 * penalties and finger configuration.

 * Created by steve on 18/10/14.
 */
public class KeyboardAnalysis {

    private final static String DEFAULT_OUTPUT_OPTIONS = "t";

    private static List<String> messages = new ArrayList<>();

    public static void main(String args[]) {
        if (args.length < 3) {
            exitHelp();
            return;
        }

        File keyboardFile = new File(args[args.length-1]);
        KeyboardLayout keyboardLayout = new KeyboardLayout();
        boolean ok = keyboardLayout.parse(keyboardFile);
        if (!ok) {
            return;
        }
        keyboardLayout.dump(System.out);

        String outputOptions = null;
        CharFreq[] cFreqs = null;
        BigramFreq[] bFreqs = null;

        FingerConfig fingerConfig = new FingerConfig();
        for (int i=0; i<args.length-1; ++i) {
            if (args[i].equals("-c")) {
                String[] configFiles = args[++i].split(",");
                for (String configFile : configFiles) {
                    fingerConfig.parse(keyboardLayout, new File(configFile));
                }

            } else if (args[i].equals("-f")) {
                String frequencyFile = args[++i];
                cFreqs = CharFreq.initialize(keyboardLayout.getAlphabet(), new File(frequencyFile));
                bFreqs = BigramFreq.initialize(keyboardLayout.getAlphabet(), new File(frequencyFile));
                if (cFreqs == null || bFreqs == null) {
                    return;
                }

            } else if (args[i].equals("-w")) {
                String wordFile = args[++i];

                FreqAnalysis fa = new FreqAnalysis(keyboardLayout.getAlphabet());
                fa.analyze(new File(wordFile));

                cFreqs = fa.getCharFreqs();
                bFreqs = fa.getBigramFreqs();

            } else if (args[i].startsWith("-o")) {
                outputOptions = args[++i];
            }
        }
        fingerConfig.dump(keyboardLayout, System.out);

        if (cFreqs == null || bFreqs == null) {
            exitHelp();
            return;
        }

        KeyboardAnalysis ka = new KeyboardAnalysis();
        LayoutResults layoutResults = ka.performAnalysis(keyboardLayout, cFreqs, bFreqs);

        try {
            //use default outputOptions if none supplied
            if (outputOptions == null) {
                outputOptions = DEFAULT_OUTPUT_OPTIONS;
            }
            //generate full text report
            if (outputOptions.indexOf('t') >= 0) {
                KeyboardAnalysisTextReport kr = new KeyboardAnalysisTextReport(10, 5);
                String output = kr.generate(layoutResults);
                System.out.println(output);
            }
            //generate brief report for html table
            if (outputOptions.indexOf('b') >= 0) {
                KeyboardAnalysisBriefHTMLReport kr = new KeyboardAnalysisBriefHTMLReport();
                String output = kr.generate(layoutResults);
                System.out.println(output);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static void exitHelp() {
        System.out.println("KeyboardAnalysis  -c configFiles..  -f frequencyFile  keyboardFile");
        System.out.println("KeyboardAnalysis  -c configFiles..  -w wordFile  keyboardFile");
        System.exit(0);
    }

    public LayoutResults performAnalysis(KeyboardLayout keyboardLayout, CharFreq[] cFreqs, BigramFreq[] bFreqs) {
        List<String> messages = sanityCheck(keyboardLayout);

        double[] fingerFreq = calculateFingerFrequency(keyboardLayout, cFreqs);

        double handAlternation = countHandAlternation(keyboardLayout, bFreqs);

        //find same-finger bigrams for the current layout
        List<FingerBigram> sameFingerBigrams = findSameFingerBigrams(keyboardLayout, bFreqs);
        List<FingerBigram> neighbourFingerBigrams = findNeighbourFingerBigrams(keyboardLayout, bFreqs);

        double[][] fingerEffort = new double[3][];
        fingerEffort[0] = calculateBaseEffort(keyboardLayout, cFreqs);
        fingerEffort[1] = calculateSameFingerBigramEffort(keyboardLayout, sameFingerBigrams);
        fingerEffort[2] = calculateNeighbourFingerBigramEffort(keyboardLayout, neighbourFingerBigrams);

        return new LayoutResults(keyboardLayout, messages, fingerFreq, handAlternation,
            sameFingerBigrams, neighbourFingerBigrams, fingerEffort);
    }

    private List<String> sanityCheck(KeyboardLayout keyboardLayout) {
        List<String> messages = new ArrayList<>();

        String alphabet = keyboardLayout.getAlphabet();
        for (char c = 'A'; c <= 'Z'; ++c) {
            if (alphabet.indexOf(c) < 0) {
                messages.add("Warning: letter " + c + " is missing from layout. ");
            }
        }
        for (int i=0; i<alphabet.length(); ++i) {
            char c = alphabet.charAt(i);
            if (alphabet.indexOf(c, i+1) >= 0) {
                messages.add("Warning: letter " + c + " appears more than once on layout. ");
            }
        }
        return messages;
    }

    private double[] calculateFingerFrequency(KeyboardLayout keyboardLayout, CharFreq[] charFreqs) {
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

    private double countHandAlternation(KeyboardLayout keyboardLayout, BigramFreq[] bigramFreqs) {
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
     * @param bigramFreqs the full set of bigram frequencies
     * @return the list of same-finger bigrams found
     */
    private List<FingerBigram> findSameFingerBigrams(KeyboardLayout keyboardLayout, BigramFreq[] bigramFreqs) {
        List<FingerBigram> fingerBigrams = new ArrayList<FingerBigram>();

        for (BigramFreq bigramFreq : bigramFreqs) {
            char[] chars = bigramFreq.getString().toCharArray();
            Key key1 = keyboardLayout.lookupKey(chars[0]);
            Key key2 = keyboardLayout.lookupKey(chars[1]);
            if (key1 != null && key2 != null) {
                if (chars[0] != chars[1] && FingerConfig.isSameFinger(key1.getFinger(), key2.getFinger())) {
                    FingerBigram fingerBigram = new FingerBigram(key1, key2, bigramFreq);
                    fingerBigrams.add(fingerBigram);
                }

            }
        }

        return fingerBigrams;
    }

    /**
     * Return a list of neighbour-finger bigrams for this layout that have defined penalties
     *
     * @param keyboardLayout the current keyboard layout
     * @param bigramFreqs the full set of bigram frequencies
     * @return the list of neighbour-finger bigrams found
     */
    private List<FingerBigram> findNeighbourFingerBigrams(KeyboardLayout keyboardLayout, BigramFreq[] bigramFreqs) {
        List<FingerBigram> fingerBigrams = new ArrayList<FingerBigram>();

        for (BigramFreq bigramFreq : bigramFreqs) {
            char[] chars = bigramFreq.getString().toCharArray();
            Key key1 = keyboardLayout.lookupKey(chars[0]);
            Key key2 = keyboardLayout.lookupKey(chars[1]);
            if (key1 != null && key2 != null) {
                if (FingerConfig.isNeighbourFinger(key1.getFinger(), key2.getFinger())) {
                    int outermostFinger = FingerConfig.getOutermostFinger(key1.getFinger(), key2.getFinger());
                    if (keyboardLayout.hasPenaltyNeighbourFinger(outermostFinger)) {
                        FingerBigram fingerBigram = new FingerBigram(key1, key2, bigramFreq);
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
    private double[] calculateBaseEffort(KeyboardLayout keyboardLayout, CharFreq[] cfreq) {
        double[] baseEffort = new double[10];
        for (char ch : keyboardLayout.getAlphabet().toCharArray()) {
            Key key = keyboardLayout.lookupKey(ch);
            CharFreq cf = CharFreq.findByChar(ch, cfreq);
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
    private double[] calculateNeighbourFingerBigramEffort(KeyboardLayout keyboardLayout, List<FingerBigram> fingerBigrams) {
        double[] bigramEffort = new double[10];
        for (FingerBigram fingerBigram : fingerBigrams) {
            int finger1 = fingerBigram.getKey1().getFinger();
            int finger2 = fingerBigram.getKey2().getFinger();
            int outermostFinger = FingerConfig.getOutermostFinger(finger1, finger2);
            int diffrows = Math.min(2, Math.abs(fingerBigram.getKey1().getRow() - fingerBigram.getKey2().getRow()));
            double penalty = keyboardLayout.getPenaltyNeighbourFinger(outermostFinger, diffrows);
            bigramEffort[outermostFinger] += fingerBigram.getBigramFreq().getFreq() * penalty;
        }
        return bigramEffort;
    }

}
