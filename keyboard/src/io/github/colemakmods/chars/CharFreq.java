package io.github.colemakmods.chars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a single character and the frequency with which it occurs in a given corpus
 */

public class CharFreq {

    public static List<CharFreq> initialize(String alphabet) {
        List<CharFreq> charFreqList = new ArrayList<>();
        for (int i=0; i< alphabet.length(); ++i) {
            char c = alphabet.charAt(i);
            charFreqList.add(new CharFreq(c));
        }
        return charFreqList;
    }

    public static List<CharFreq> initialize(String alphabet, File file) {
        try {
            return initialize(alphabet, new FileReader(file));
        } catch (IOException ex) {
            System.err.println("Unable to load char frequency file");
            ex.printStackTrace();
            return null;
        }
    }

    public static List<CharFreq> initialize(String alphabet, String data) {
        try {
            return initialize(alphabet, new StringReader(data));
        } catch (IOException ex) {
            System.err.println("Unable to load char frequency string");
            ex.printStackTrace();
            return null;
        }
    }

    private static List<CharFreq> initialize(String alphabet, Reader in) throws IOException {
        String line;
        List<CharFreq> charFreqList = new ArrayList<CharFreq>();
        BufferedReader br = new BufferedReader(in);
        try {
            while ((line = br.readLine()) != null) {
                List<String> tokens = StringSplitter.split(line, ' ');
                if (!line.startsWith("#") && tokens.size() >= 2) {
                    String ch = tokens.get(0).toUpperCase();
                    if (ch.length() == 1) {
                        if (alphabet.contains(ch)) {
                            long count = Long.parseLong(tokens.get(1));
                            charFreqList.add(new CharFreq(ch.charAt(0), count));
                        }
                    }
                }
            }
        } finally {
            br.close();
        }
        normalize(charFreqList);
        //System.out.printf("Read %d char freq\n", cfreq.length);
        return charFreqList;
    }

    public static void normalize(List<CharFreq> charFreqList) {
        long total = 0;
        for (CharFreq cf : charFreqList) {
            total += cf.getCount();
        }
        for (CharFreq cf : charFreqList) {
            cf.freq = (double) cf.getCount() / total;
        }
    }

    public static CharFreq findByChar(char c, List<CharFreq> charFreqList) {
        for (CharFreq cf : charFreqList) {
            if (Character.toUpperCase(c) == cf.getChar()) {
                return cf;
            }
        }
        return null;
    }

    
    private char ch;
    private long count;
    private double freq;
    
    public CharFreq(char ch) {
        this.ch = ch;
    }

    public CharFreq(char ch, long count) {
        this.ch = ch;
        this.count = count;
    }

    public char getChar() {
        return ch;
    }
    
    public void addCount() {
        count++;
    }
    
    public long getCount() {
        return count;
    }

    public double getFreq() {
        return freq;
    }

    public static class CharFreqComparer implements Comparator<CharFreq> {
        @Override
        public int compare(CharFreq cf1, CharFreq cf2) {
            if (cf1.count > cf2.count) {
                return -1;
            } else if (cf1.count < cf2.count) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
