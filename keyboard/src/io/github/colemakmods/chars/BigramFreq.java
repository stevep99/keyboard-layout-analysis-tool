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
 * Represents a bigram (pair of consecutive letters) and the frequency with which that bigram occurs in a given corpus
 */
public class BigramFreq {

    public static BigramFreq[] initialize(String alphabet) {
        BigramFreq[] bfreq = new BigramFreq[alphabet.length() * alphabet.length()];
        int c = 0;
        for (int i=0; i< alphabet.length(); ++i) {
            for (int j=0; j< alphabet.length(); ++j) {
                String str = new StringBuilder().append(alphabet.charAt(i)).append(alphabet.charAt(j)).toString();
                bfreq[c++] = new BigramFreq(str);
            }
        }
        return bfreq;
    }

    public static BigramFreq[] initialize(String alphabet, File file) {
        try {
            return initialize(alphabet, new FileReader(file));
        } catch (IOException ex) {
            System.err.println("Unable to load bigram frequency file");
            ex.printStackTrace();
            return null;
        }
    }

    public static BigramFreq[] initialize(String alphabet, String data) {
        try {
            return initialize(alphabet, new StringReader(data));
        } catch (IOException ex) {
            System.err.println("Unable to load bigram frequency string");
            ex.printStackTrace();
            return null;
        }
    }

    private static BigramFreq[] initialize(String alphabet, Reader in) throws IOException {
        String line;
        List<BigramFreq> bfreqList = new ArrayList<BigramFreq>();
        BufferedReader br = new BufferedReader(in);
        try {
            while ((line = br.readLine()) != null) {
                List<String> tokens = StringSplitter.split(line, ' ');
                if (!line.startsWith("#") && tokens.size() >= 2) {
                    String bigram = tokens.get(0).toUpperCase();
                    if (bigram.length() == 2) {
                        if (alphabet.indexOf(bigram.charAt(0)) >= 0 && alphabet.indexOf(bigram.charAt(1)) >= 0) {
                            long count = Long.parseLong(tokens.get(1));
                            bfreqList.add(new BigramFreq(bigram, count));
                        }
                    }
                }
            }
        } finally {
            br.close();
        }
        BigramFreq[] bfreq = bfreqList.toArray(new BigramFreq[bfreqList.size()]);
        normalize(bfreq);
        //System.out.printf("Read %d bigram freq\n", bfreq.length);
        return bfreq;
    }

    public static void normalize(BigramFreq[] bfreq) {
        long total = 0;
        for (int i=0; i< bfreq.length; ++i) {
            total += bfreq[i].getCount();
        }
        for (int i=0; i< bfreq.length; ++i) {
            bfreq[i].freq = (double) bfreq[i].getCount() / total;
        }
    }

    public static BigramFreq findByString(String str, BigramFreq[] bfreq) {
        for (int i=0; i< bfreq.length; ++i) {
            if (bfreq[i].str.equals(str)) {
                return bfreq[i];
            }
        }
        return null;
    }

    private String str;
    private long count;
    private double freq;

    public BigramFreq(String str) {
        this.str = str;
    }

    public BigramFreq(String str, long count) {
        this.str = str;
        this.count = count;
    }

    public String getString() {
        return str;
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

    public static class BigramFreqComparer implements Comparator<BigramFreq> {
        @Override
        public int compare(BigramFreq bf1, BigramFreq bf2) {
            if (bf1.freq > bf2.freq) {
                return -1;
            } else if (bf1.freq < bf2.freq) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
