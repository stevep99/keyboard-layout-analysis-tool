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

        String keyboard1File = args[args.length - 2];
        KeyboardLayout keyboardLayout1 = new KeyboardLayout(keyboard1File);
        boolean ok1 = KeyboardMapping.parse(keyboardLayout1, new File(keyboard1File));
        if (!ok1) {
            return;
        }
        keyboardLayout1.dumpLayout(System.out);

        String keyboard2File = args[args.length - 1];
        KeyboardLayout keyboardLayout2 = new KeyboardLayout(keyboard2File);
        boolean ok2 = KeyboardMapping.parse(keyboardLayout2, new File(keyboard1File));
        if (!ok2) {
            return;
        }
        keyboardLayout2.dumpLayout(System.out);

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
        System.out.println("KeyboardCompare  -f frequencyFile  [-p positionOverrideFile]  keyboard1File  keyboard2File");
        System.exit(0);
    }

    public void performCompare(KeyboardLayout keyboardLayout1, KeyboardLayout keyboardLayout2, List<CharFreq> charFreqs, Properties posOverrides, PrintStream out) {
        ExperienceCurve experienceCurve = new ExperienceCurve(EXP_B_PARAM);

        double totalPosDiff = 0f;
        double totalFreq = 0f;
        double totalScore = 0f;
        int[] count = new int[6];

        out.println();
        for (char ch : keyboardLayout1.getAlphabet().toCharArray()) {
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
                    posdiff = 1;
                    count[4]++;
                }
            } else {
                posdiff = 1;
                count[4]++;
            }
            totalPosDiff += posdiff;

            CharFreq cf = CharFreq.findByChar(ch, charFreqs);
            if (cf != null) {
                double freqCost = experienceCurve.getIntegratedCost(100*cf.getFreq());
                double score = posdiff * freqCost;
                out.printf("%s:  %f  %f  %f  %f\n", ch, posdiff, cf.getFreq(), freqCost, score);
                totalFreq += cf.getFreq();
                totalScore += score;
            }
        }

        out.println();
        out.printf("*: %f %f %f\n", totalPosDiff, totalFreq, totalScore);

        out.println();
        out.printf("SH / SF : %d\n", count[0]);
        out.printf("SH / SF : %d\n", count[1]);
        out.printf("SH / DF : %d\n", count[2]);
        out.printf("DH / SF : %d\n", count[3]);
        out.printf("DH / DF : %d\n", count[4]);
        if (posOverrides != null) {
            out.printf("Override: %d\n", count[5]);
        }
    }

}
