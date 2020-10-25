package io.github.colemakmods.keyboard;

import io.github.colemakmods.chars.StringSplitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.Exception;
import java.util.List;

/**
 * The type of finger configuration used for typing. For definitions, see
 * http://colemakmods.github.io/mod-dh/analyze.html
 *
 * Created by steve on 20/10/14.
 */
public class KeyboardConfig {

    public enum Section {FINGERS, EFFORT, PENALTIES, TYPE}

    public static boolean parse(KeyboardLayout keyboardLayout, String data) {
        try {
            return parse(keyboardLayout, new StringReader(data));
        } catch (IOException ex) {
            System.err.println("Unable to load finger key definitions file");
            ex.printStackTrace();
            return false;
        }
    }

    public static boolean parse(KeyboardLayout keyboardLayout, File file) {
        try {
            return parse(keyboardLayout, new FileReader(file));
        } catch (IOException ex) {
            System.err.println("Unable to load finger key definitions file");
            ex.printStackTrace();
            return false;
        }
    }

    private static boolean parse(KeyboardLayout keyboardLayout, Reader reader) throws IOException {
        Section section = Section.FINGERS;
        int sectionRows = 3;
        BufferedReader br = new BufferedReader(reader);
        String line;
        try {
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
                if (line.contains(":")) {
                    List<String> tokens = StringSplitter.split(line, ':');
                    try {
                        section = Section.valueOf(tokens.get(0).toUpperCase());
                        if (tokens.size() > 1) {
                            sectionRows = Integer.parseInt(tokens.get(1).trim());
                        } else {
                            sectionRows = 3;
                        }
                        row = 0;
                    } catch (Exception e) {
                        System.err.println("Invalid section " + tokens.get(0));
                    }
                    continue;
                }
                if (section == Section.EFFORT) {
                    List<String> tokens = StringSplitter.split(line, ' ');
                    for (int col = 0; col < tokens.size(); col++) {
                        int keyboardRow = row + (keyboardLayout.getRows() - sectionRows);
                        if (keyboardRow >= 0) {
                            Key key = keyboardLayout.lookupKey(keyboardRow, col);
                            if (key != null) {
                                key.setEffort(Double.parseDouble(tokens.get(col)));
                            }
                        }
                    }
                    ++row;
                } else if (section == Section.FINGERS) {
                    List<String> tokens = StringSplitter.split(line, ' ');
                    for (int col = 0; col < tokens.size(); col++) {
                        int keyboardRow = row + (keyboardLayout.getRows() - sectionRows);
                        if (keyboardRow >= 0) {
                            Key key = keyboardLayout.lookupKey(keyboardRow, col);
                            if (key != null) {
                                key.setFinger(Integer.parseInt(tokens.get(col)));
                            }
                        }
                    }
                    ++row;
                } else if (section == Section.PENALTIES) {
                    List<String> tokens = StringSplitter.split(line, ' ');
                    if (row == 0) {
                        //same-finger penalty
                        for (int rowdiff = 0; rowdiff < tokens.size(); rowdiff++) {
                            keyboardLayout.setPenaltySameFinger(rowdiff, Double.parseDouble(tokens.get(rowdiff)));
                        }
                    } else {
                        //neighbour-finger penalty
                        int outermostFinger = row - 1;
                        for (int rowdiff = 0; rowdiff < tokens.size(); rowdiff++) {
                            keyboardLayout.setPenaltyNeighbourFinger(outermostFinger, rowdiff, Double.parseDouble(tokens.get(rowdiff)));
                        }
                    }
                    ++row;
                } else if (section == Section.TYPE) {
                    List<String> tokens = StringSplitter.split(line, ' ');
                    try {
                        keyboardLayout.setKeyboardType(KeyboardLayout.KeyboardType.valueOf(tokens.get(0).toUpperCase()));
                    } catch (Exception ex) {
                        throw new IOException("Invalid KeyboardType " + tokens.get(0));
                    }
                    ++row;
                }
            }
            return true;
        } finally {
            br.close();
        }
    }

    public static boolean isSameFinger(int f1, int f2) {
        return f1 == f2;
    }

    public static boolean isNeighbourFinger(int f1, int f2) {
        return (f1 - f2 == 1) || (f1 - f2 == -1);
    }

    public static int getOutermostFinger(int f1, int f2) {
        int left1 = (f1 <= 4) ? f1 : 9-f1;
        int left2 = (f2 <= 4) ? f2 : 9-f2;
        return (left1 < left2) ? f1 : f2;
    }

}
