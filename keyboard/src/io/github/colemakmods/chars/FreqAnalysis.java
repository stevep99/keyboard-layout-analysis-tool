package io.github.colemakmods.chars;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.lang.Character;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Perform a frequency analysis on some input text (corpus), and generate as output the
 * character and bigram frequencies encountered.
 */
public class FreqAnalysis {

	private final static String DEFAULT_ALPHABET = 
			"ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final static int DEFAULT_MAX_UNICODE = 0x10FFFF;
	
    private List<CharFreq> charFreqList;
    private List<BigramFreq> bigramFreqList;
    private int maxUnicode = DEFAULT_MAX_UNICODE;

    public FreqAnalysis(String alphabet, int maxUnicode) {
        this.charFreqList = CharFreq.initialize(alphabet);
        this.bigramFreqList = BigramFreq.initialize(alphabet);
        if (maxUnicode > 0) {
            this.maxUnicode = maxUnicode;
        }
    }

    public long analyze(File file) {
        long i = -1L;
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

    public long analyze(String ins) {
        long i = -1L;
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
    
    private long analyze(Reader in) throws IOException {
        int k;
        char c = ' ';
        char cp;
        long i = 0L;

        //set up caches for faster reading
        HashMap<Character, CharFreq> cCache = new HashMap<>();
        for (CharFreq cf : charFreqList) {
            cCache.put(cf.getChar(), cf);
        }
        HashMap<String, BigramFreq> bCache = new HashMap<>();
        for (BigramFreq bf : bigramFreqList) {
            bCache.put(bf.getString(), bf);
        }

        do {
            k = in.read();
            if (k < 0) break;

            cp = c;
            c = Character.toUpperCase((char) k);
            if (k > 32) { //skip any control/non-printable characters
                CharFreq cf = cCache.get(c);
                if (cf != null) {
                    cf.addCount();
                } else if (k < maxUnicode && k != 65533) {
                    //for characters: add new item to list if new character is within requested unicode range
                    cf = new CharFreq((char)k, 1);
                    charFreqList.add(cf);
                    cCache.put(cf.getChar(), cf);
                }
                String bigram = new StringBuilder().append(cp).append(c).toString().toUpperCase();
                BigramFreq bf = bCache.get(bigram);
                if (bf != null) {
                    bf.addCount();
                }
            }

            ++i;
            if (i % 10240 == 0) {
                System.err.print("Read " + (i/1024) + " kb\r");
            }
        } while (k >= 0);

        System.err.print("Read " + (i/1024) + " kb (complete)\r");
        sort();

        return i;
    }

    private void sort() {
        Collections.sort(charFreqList, new CharFreq.CharFreqComparer());
        Collections.sort(bigramFreqList, new BigramFreq.BigramFreqComparer());
    }

    private void normalize() {
        CharFreq.normalize(charFreqList);
        BigramFreq.normalize(bigramFreqList);
    }

    public List<CharFreq> getCharFreqs() {
        return charFreqList;
    }

    public List<BigramFreq> getBigramFreqs() {
        return bigramFreqList;
    }

    public void showFrequencyAnalysis(boolean showNormalized) {
        for (CharFreq cf : charFreqList) {
            if (showNormalized) {
                System.out.println(cf.getChar() + " " + cf.getCount() + " " + cf.getFreq());
            } else {
                System.out.println(cf.getChar() + " " + cf.getCount());
            }
        }
        for (BigramFreq bf : bigramFreqList) {
            if (showNormalized) {
                System.out.println(bf.getString() + " " + bf.getCount() + " " + bf.getFreq());
            } else {
                System.out.println(bf.getString() + " " + bf.getCount());
            }
        }
    }

    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("Usage:  FreqAnalysis  [-f]  [-a alphabet]  [-u max_unicode] filename");
            System.exit(0);
        }

        String wordFile = args[args.length-1];
        String alphabet = DEFAULT_ALPHABET;
        int maxUnicode = -1;
        boolean showNormalized = false;

        for (int i=0; i<args.length-1; ++i) {
            if (args[i].equals("-a")) {
                alphabet = args[++i].toUpperCase();
            } else if (args[i].equals("-u")) {
                maxUnicode = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-f")) {
                showNormalized = true;
            }
        }

        long timeStart = System.currentTimeMillis();
        FreqAnalysis fa = new FreqAnalysis(alphabet, maxUnicode);
        long i = fa.analyze(new File(wordFile));
        if (showNormalized) {
            fa.normalize();
        }
        long timeEnd = System.currentTimeMillis();

        if (i > 0) {
            fa.showFrequencyAnalysis(showNormalized);
            System.err.println("Read " + (i/1024) + " kb in " + (timeEnd - timeStart) + "ms");
        }

    }
}
