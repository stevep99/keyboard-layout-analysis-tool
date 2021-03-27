package io.github.colemakmods.keyboard;

import io.github.colemakmods.chars.CharFreq;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

/**
 * Perform a comparison of a keyboard layout for differences against a base layout (usually Qwerty)
 *
 * Created by steve on 28/11/14.
 */
public class KeyboardCompare {

    private final static double EXP_B_PARAM = 0.8f;

    public static void main(String args[]) {
        if (args.length < 3) {
            exitHelp();
            return;
        }

        String keyboard1Filename = args[args.length - 4];
        KeyboardLayout keyboardLayout1 = new KeyboardLayout(keyboard1Filename);
        boolean ok1 = KeyboardMapping.parse(keyboardLayout1, new File(keyboard1Filename));
        if (!ok1) {
            return;
        }
        String config1Filename = args[args.length - 3];
        KeyboardConfig.parse(keyboardLayout1, new File(config1Filename));
        keyboardLayout1.dumpLayout(System.out);
        keyboardLayout1.dumpConfig(System.out);

        String keyboard2Filename = args[args.length - 2];
        KeyboardLayout keyboardLayout2 = new KeyboardLayout(keyboard2Filename);
        boolean ok2 = KeyboardMapping.parse(keyboardLayout2, new File(keyboard2Filename));
        if (!ok2) {
            return;
        }
        String config2Filename = args[args.length - 1];
        KeyboardConfig.parse(keyboardLayout2, new File(config2Filename));
        keyboardLayout2.dumpLayout(System.out);
        keyboardLayout2.dumpConfig(System.out);

        List<CharFreq> charFreqs = null;
        Properties posOverrides = null;
        for (int i = 0; i < args.length - 1; ++i) {
            if (args[i].equals("-f")) {
                String frequencyFile = args[i + 1];
                charFreqs = CharFreq.initialize(keyboardLayout1.getAlphabet(), new File(frequencyFile));
            } else if (args[i].equals("-p")) {
                String posFile = args[i + 1];
                posOverrides = new Properties();
                try {
                    posOverrides.load(new FileReader(posFile));
                } catch (IOException ex) {
                    System.err.println("Unable to load position-override file");
                    ex.printStackTrace();
                    return;
                }
            }
        }

        if (charFreqs == null) {
            return;
        }

        KeyboardCompare kc = new KeyboardCompare();
        kc.performCompare(keyboardLayout1, keyboardLayout2, charFreqs, posOverrides, System.out);
    }

    private static void exitHelp() {
        System.out.println("KeyboardCompare  -f frequencyFile  [-p positionOverrideFile]  keyboard1File  config1File  keyboard2File  config2File");
        System.exit(0);
    }

    public void performCompare(KeyboardLayout keyboardLayout1, KeyboardLayout keyboardLayout2, List<CharFreq> charFreqs, Properties posOverrides, PrintStream out) {
        ExperienceCurve experienceCurve = new ExperienceCurve(EXP_B_PARAM);

        double totalPosDiff = 0f;
        double totalFreq = 0f;
        double totalScore = 0f;
        int[] count = new int[6];

        out.println();
        for (char ch : keyboardLayout1.getPrimaryChars().toUpperCase().toCharArray()) {
            Key key1 = keyboardLayout1.lookupKey(ch);
            Key key2 = keyboardLayout2.lookupKey(ch);

            double posdiff;
            String valOverride = (posOverrides != null) ? (String) posOverrides.get(String.valueOf(ch)) : null;
            if (valOverride != null) {
                posdiff = Double.parseDouble(valOverride);
                count[5]++;
            } else if (key1 != null && key2 != null) {
                if (key1.getCol() == key2.getCol() && key1.getRow() == key2.getRow()) {
                    posdiff = 0;
                    count[0]++;
                } else if (key1.getFinger() == key2.getFinger()) {
                    posdiff = 0.25;
                    count[1]++;
                } else if (key1.getHand() == key2.getHand()) {
                    posdiff = 0.5;
                    count[2]++;
                } else if (key1.getFinger() + key2.getFinger() == 9) {
                    posdiff = 0.75;
                    count[3]++;
                } else {
                    posdiff = 1.0;
                    count[4]++;
                }
            } else {
                posdiff = 1;
                count[4]++;
            }
            totalPosDiff += posdiff;

            double effortRatio = 1.0;
            if (key1 != null && key2 != null) {
                effortRatio = key2.getEffort() / key1.getEffort();
            }

            CharFreq cf = CharFreq.findByChar(ch, charFreqs);
            if (cf != null) {
                double freqCost = experienceCurve.getIntegratedCost(100*cf.getFreq());
                double score = posdiff * freqCost * effortRatio;
                out.printf("%s:  %f  %f  %f  %f  %f\n", ch, posdiff, cf.getFreq(), freqCost, effortRatio, score);
                totalFreq += cf.getFreq();
                totalScore += score;
            }
        }

        out.println();
        out.printf("*: %f  %f  %f\n", totalPosDiff, totalFreq, totalScore);

        out.println();
        int totalMoved = count[1] + count[2] + count[3] + count[4];
        out.printf("Keys moved : %d\n", totalMoved);
        out.printf("  Unchanged: %d\n\n", count[0]);
        out.printf(" SH / SF : %d\n", count[1]);
        out.printf(" SH / DF : %d\n", count[2]);
        out.printf(" DH / SF : %d\n", count[3]);
        out.printf(" DH / DF : %d\n", count[4]);
        if (posOverrides != null) {
            out.printf("Override: %d\n", count[5]);
        }
    }

}
