package io.github.colemakmods.keyboard;

import io.github.colemakmods.chars.StringSplitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the keyboard layout being analyzed
 *
 * Created by steve on 18/10/14.
 */
public class KeyboardLayout {

    public enum KeyboardType {
        STD, ANGLE, MATRIX
    }

    private String name;
    private List<Key> keyList = new ArrayList<Key>();
    private int cols;
    private int rows;
    private double[] penaltySameFinger = new double[3];
    private double[][] penaltyNeighbourFinger = new double[4][3];
    private KeyboardType keyboardType = KeyboardType.STD;

    public boolean parse(File file) {
        try {
            this.name = file.getName();
            parse(new FileReader(file));
            return validate();
        } catch (Exception ex) {
            System.err.println("Unable to load layout file");
            ex.printStackTrace();
            return false;
        }
    }

    public boolean parse(String input, String name) {
        try {
            this.name = name;
            parse(new StringReader(input));
            return validate();
        } catch (Exception ex) {
            System.err.println("Unable to read layout data");
            ex.printStackTrace();
            return false;
        }
    }

    private void parse(Reader in) throws IOException {
        BufferedReader br = new BufferedReader(in);
        try {
            String line;
            int row = 0;
            while ((line = br.readLine()) != null) {
                int commentpos = line.indexOf('#');
                if (commentpos >= 0) {
                    line = line.substring(0, commentpos);
                }
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                List<String> chars = StringSplitter.split(line, ' ');
                for (int col = 0; col < chars.size(); col++) {
                    Key key = new Key(chars.get(col), row, col);
                    keyList.add(key);
                }
                if (chars.size() > cols) {
                    cols = chars.size();
                }
                ++row;
                if (row > rows) {
                    rows = row;
                }
            }
        } finally {
            br.close();
        }
    }

    private boolean validate() {
        //check
        for (int i=0; i < keyList.size(); ++i) {
            for (int j=0; j < keyList.size(); ++j) {
                if (i != j) {
                    if (keyList.get(j).hasChar(keyList.get(i).getName())) {
                        return false;
                    }
                }
            }
        }
        return true;
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

    public void dump(PrintStream out) {
        out.println();
        out.println("Keyboard with " + keyList.size() + " keys:");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Key key = lookupKey(r, c);
                if (key != null) {
                    out.print(key.getName() + " ");
                } else {
                    out.print("   ");
                }
            }
            out.println();
        }
        //out.println("Alphabet " + getAlphabet() + "\n");
    }
}
