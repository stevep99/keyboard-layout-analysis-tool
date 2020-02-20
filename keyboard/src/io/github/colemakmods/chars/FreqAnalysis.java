package io.github.colemakmods.chars;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.lang.Character;
import java.util.Arrays;

/**
 * Perform a frequency analysis on some input text (corpus), and generate as output the
 * character and bigram frequencies encountered.
 */
public class FreqAnalysis {

	private final static String DEFAULT_ALPHABET = 
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
    private CharFreq[] cfreq;
    private BigramFreq[] bfreq;

    public FreqAnalysis(String alphabet) {
        cfreq = CharFreq.initialize(alphabet);
        bfreq = BigramFreq.initialize(alphabet);
    }

    public int analyze(File file) {
        int i = -1;
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            try {
                i = analyze(in);
            } finally {
                in.close();
            }
        } catch(IOException ex) {
            System.err.println("Error reading from " + file);
            ex.printStackTrace();
        }
        return i;
    }

    public int analyze(String ins) {
        int i = -1;
        try {
            StringReader in  = new StringReader(ins);
            try {
                i = analyze(in);
            } finally {
                in.close();
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
        return i;
    }
    
    private int analyze(Reader in) throws IOException {
        int k;
        char c = ' ';
        char cp;
        int i = 0;

        do {
            k = in.read();
            if (k >= 0) {
                cp = c;
                c = Character.toUpperCase((char) k);
                CharFreq cf = CharFreq.findByChar(c, cfreq);
                if (cf != null) {
                    cf.addCount();
                }
                String bigram = new StringBuilder().append(cp).append(c).toString().toUpperCase();
                BigramFreq df = BigramFreq.findByString(bigram, bfreq);
                if (df != null) {
                    df.addCount();
                }

                ++i;
                if (i % 10000 == 0) sort();
            }
        } while (k >= 0);

        sort();

        CharFreq.normalize(cfreq);
        BigramFreq.normalize(bfreq);

        return i;
    }

    private void sort() {
        Arrays.sort(cfreq, new CharFreq.CharFreqComparer());
        Arrays.sort(bfreq, new BigramFreq.BigramFreqComparer());
    }

    public CharFreq[] getCharFreqs() {
        return cfreq;
    }

    public BigramFreq[] getBigramFreqs() {
        return bfreq;
    }

    public void showFrequencyAnalysis(boolean showNormalized) {
        for (int i = 0; i < cfreq.length; ++i) {
            CharFreq cf = cfreq[i];
            if (showNormalized) {
                System.out.println(cf.getChar() + " " + cf.getCount() + " " + cf.getFreq());
            } else {
                System.out.println(cf.getChar() + " " + cf.getCount());
            }
        }
        for (int i = 0; i < bfreq.length; ++i) {
            BigramFreq df = bfreq[i];
            if (showNormalized) {
                System.out.println(df.getString() + " " + df.getCount() + " " + df.getFreq());
            } else {
                System.out.println(df.getString() + " " + df.getCount());
            }
        }
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("Usage:  FreqAnalysis  [-f]  [-a alphabet]  filename");
            System.exit(0);
        }

        String wordFile = args[args.length-1];
        String alphabet = DEFAULT_ALPHABET;
        boolean showNormalized = false;

        for (int i=0; i<args.length-1; ++i) {
            if (args[i].equals("-a")) {
                alphabet = args[++i].toUpperCase();
            }
            if (args[i].equals("-f")) {
                showNormalized = true;
            }
        }

        long timeStart = System.currentTimeMillis();
        FreqAnalysis fa = new FreqAnalysis(alphabet);
        int i = fa.analyze(new File(wordFile));
        long timeEnd = System.currentTimeMillis();

        if (i > 0) {
            fa.showFrequencyAnalysis(showNormalized);
            System.err.println("Read " + i + " characters in " + (timeEnd - timeStart) + "ms");
        }

    }
}
