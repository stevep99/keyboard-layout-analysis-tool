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
    
    public static CharFreq[] initialize(String alphabet) {
        CharFreq[] charfreq = new CharFreq[alphabet.length()];
        for (int i=0; i< alphabet.length(); ++i) {
            char c = alphabet.charAt(i);
            charfreq[i] = new CharFreq(c);
        }
        return charfreq;
    }

    public static CharFreq[] initialize(String alphabet, File file) {
        try {
            return initialize(alphabet, new FileReader(file));
        } catch (IOException ex) {
            System.err.println("Unable to load char frequency file");
            ex.printStackTrace();
            return null;
        }
    }

    public static CharFreq[] initialize(String alphabet, String data) {
        try {
            return initialize(alphabet, new StringReader(data));
        } catch (IOException ex) {
            System.err.println("Unable to load char frequency string");
            ex.printStackTrace();
            return null;
        }
    }

    private static CharFreq[] initialize(String alphabet, Reader in) throws IOException {
        String line;
        List<CharFreq> cfreqList = new ArrayList<CharFreq>();
        BufferedReader br = new BufferedReader(in);
        try {
            while ((line = br.readLine()) != null) {
                List<String> tokens = StringSplitter.split(line, ' ');
                if (!line.startsWith("#") && tokens.size() >= 2) {
                    String ch = tokens.get(0).toUpperCase();
                    if (ch.length() == 1) {
                        if (alphabet.indexOf(ch) >= 0) {
                            long count = Long.parseLong(tokens.get(1));
                            cfreqList.add(new CharFreq(ch.charAt(0), count));
                        }
                    }
                }
            }
        } finally {
            br.close();
        }
        CharFreq[] cfreq = cfreqList.toArray(new CharFreq[cfreqList.size()]);
        normalize(cfreq);
        //System.out.printf("Read %d char freq\n", cfreq.length);
        return cfreq;
    }

    public static void normalize(CharFreq[] charfreq) {
        long total = 0;
        for (int i=0; i< charfreq.length; ++i) {
            total += charfreq[i].getCount();
        }
        for (int i=0; i< charfreq.length; ++i) {
            charfreq[i].freq = (double) charfreq[i].getCount() / total;
        }
    }

    public static CharFreq findByChar(char c, CharFreq[] charfreq) {
        for (int i=0; i< charfreq.length; ++i) {
            if (Character.toUpperCase(c) == charfreq[i].getChar()) {
                return charfreq[i];
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
            if (cf1.freq > cf2.freq) {
                return -1;
            } else if (cf1.freq < cf2.freq) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
