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

    public boolean analyze(File file) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            try {
                analyze(in);
                showFrequencyAnalysis();
            } finally {
                in.close();
            }
            return true;
        } catch(IOException ex) {
            System.err.println("Error reading from " + file);
            ex.printStackTrace();
            return false;
        }
    }

    public void analyze(String ins) {
        try {
            StringReader in  = new StringReader(ins);
            try {
                analyze(in);
            } finally {
                in.close();
            }
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }
    
    private void analyze(Reader in) throws IOException {
        int k;
        char c = ' ';
        char cp;

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
            }
        } while (k >= 0);

        CharFreq.normalize(cfreq);
        BigramFreq.normalize(bfreq);

        Arrays.sort(cfreq, new CharFreq.CharFreqComparer());
        Arrays.sort(bfreq, new BigramFreq.BigramFreqComparer());
    }

    public CharFreq[] getCharFreqs() {
        return cfreq;
    }

    public BigramFreq[] getBigramFreqs() {
        return bfreq;
    }

    public void showFrequencyAnalysis() {
        for (int i = 0; i < cfreq.length; ++i) {
            CharFreq cf = cfreq[i];
            System.out.println(cf.getChar() + " " + cf.getCount() + " " + cf.getFreq());
        }
        for (int i = 0; i < bfreq.length; ++i) {
            BigramFreq df = bfreq[i];
            System.out.println(df.getString() + " " + df.getCount() + " " + df.getFreq());
        }
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("Usage:  FreqAnalysis  [-a alphabet]  filename");
            System.exit(0);
        }

        String wordFile = args[args.length-1];
        String alphabet = DEFAULT_ALPHABET;

        for (int i=0; i<args.length-1; ++i) {
            if (args[i].equals("-a")) {
                alphabet = args[++i].toUpperCase();
            }
        }

        FreqAnalysis fa = new FreqAnalysis(alphabet);
        fa.analyze(new File(wordFile));

    }
}
