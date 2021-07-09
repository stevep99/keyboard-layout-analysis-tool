package io.github.colemakmods.keyboard;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the keyboard layout being analyzed
 *
 * Created by steve on 18/10/14.
 */
public class KeyboardLayout {

    public enum KeyboardType {
        STD, ANGLE, MATRIX_SIMPLE, MATRIX_ERGODOX,
    }

    public final static String ALPHAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private String name;
    private List<Key> keyList = new ArrayList<>();
    private int cols;
    private int rows;
    private double[] penaltySameFinger = new double[3];
    private double[][] penaltyNeighbourFinger = new double[4][3];
    private KeyboardType keyboardType = KeyboardType.STD;

    public KeyboardLayout(String name) {
        this.name = name;
    }

    public void addKey(int row, int col, String chars) {
        Key key = new Key(row, col, chars);
        keyList.add(key);
        if (cols < key.getCol() + 1) {
            this.cols = key.getCol() + 1;
        }
        if (rows < key.getRow() + 1) {
            this.rows = key.getRow() + 1;
        }
    }

    public List<String> validate() {
        String alphabet = getAlphabet();
        List <String> errors = new ArrayList<>();
        //check for duplicate characters
        for (char alpha : ALPHAS.toCharArray()) {
            if (alphabet.indexOf(alpha) < 0) {
                errors.add("• Symbol " + alpha + " has no mapping");
            }
        }
        for (int i=0; i < alphabet.length()-1; ++i) {
            for (int j=i+1; j < alphabet.length(); ++j) {
                if (alphabet.charAt(i) == alphabet.charAt(j)) {
                    errors.add("• Symbol " + alphabet.charAt(i) + " has duplicate mapping");
                }
            }
        }
        return errors;
    }

    public Key lookupKey(char c) {
        for (Key key : keyList) {
            if (key.hasChar(c)) {
                return key;
            }
        }
        return null;
    }

    public Key lookupKey(int r, int c) {
        for (Key key : keyList) {
            if (key.getRow() == r && key.getCol() == c) {
                return key;
            }
        }
        return null;
    }

    public String getAlphabet() {
        StringBuilder sb = new StringBuilder();
        for (Key key : keyList) {
            sb.append(key.getChars());
        }
        return sb.toString();
    }

    public String getPrimaryChars() {
        StringBuilder sb = new StringBuilder();
        for (Key key : keyList) {
            sb.append(key.getPrimaryChar());
        }
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public int getCols() {
        return cols;
    }

    public int getRows() {
        return rows;
    }

    public double getPenaltySameFinger(int rowdiff) {
        return penaltySameFinger[rowdiff];
    }

    public void setPenaltySameFinger(int rowdiff, double penaltyAmount) {
        this.penaltySameFinger[rowdiff] = penaltyAmount;
    }

    public double getPenaltyNeighbourFinger(int outermostFinger, int rowdiff) {
        int outermostFingerLeft = (outermostFinger <= 4) ? outermostFinger : 9 - outermostFinger;
        if (penaltyNeighbourFinger.length <= outermostFingerLeft) return 0;
        return penaltyNeighbourFinger[outermostFingerLeft][rowdiff];
    }

    public void setPenaltyNeighbourFinger(int outermostFinger, int rowdiff, double penaltyAmount) {
        this.penaltyNeighbourFinger[outermostFinger][rowdiff] = penaltyAmount;
    }

    public boolean hasPenaltyNeighbourFinger(int outermostFinger) {
        int outermostFingerLeft = (outermostFinger <= 4) ? outermostFinger : 9 - outermostFinger;
        if (penaltyNeighbourFinger.length <= outermostFingerLeft) return false;
        for (int r = 0; r < 3; ++r) {
            if (penaltyNeighbourFinger[outermostFingerLeft][r] > 0) return true;
        }
        return false;
    }

    public KeyboardLayout.KeyboardType getKeyboardType() {
        return keyboardType;
    }

    public void setKeyboardType(KeyboardLayout.KeyboardType keyboardType) {
        this.keyboardType = keyboardType;
    }

    public void dumpLayout(PrintStream out) {
        out.println();
        out.println("Keyboard '" + name + "' with " + keyList.size() + " keys in " + rows + " rows:");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Key key = lookupKey(r, c);
                if (key != null) {
                    out.printf("  %s  ", key.getName());
                } else {
                    out.print("   ");
                }
            }
            out.println();
        }
        out.println("Alphabet " + getAlphabet());
        out.println();
    }

    public void dumpConfig(PrintStream out) {
        out.println("Effort");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Key key = lookupKey(r, c);
                if (key != null) {
                    out.printf(" % .1f ", key.getEffort());
                } else {
                    out.print("     ");
                }
            }
            out.println();
        }
        out.println("Fingers");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Key key = lookupKey(r, c);
                if (key != null) {
                    out.printf(" %s ", key.getFinger());
                } else {
                    out.print("   ");
                }
            }
            out.println();
        }
        out.println("Same-Finger Penalties");
        for (int rowdiff = 0; rowdiff < 3; ++rowdiff) {
            out.printf(" % .1f ", getPenaltySameFinger(rowdiff));
        }
        out.println();
        out.println("Neighbour-Finger Penalties");
        for (int f = 0; f < 3; ++f) {
            for (int rowdiff = 0; rowdiff < 3; ++rowdiff) {
                out.printf(" % .1f ", getPenaltyNeighbourFinger(f, rowdiff));
            }
            out.println();
        }
        out.println();
    }
}
