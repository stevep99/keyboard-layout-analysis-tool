package io.github.colemakmods.chars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by steve on 19/01/17.
 */

public class SpeechAnalysis {

    private final static int MIN_WORD_LENGTH = 1;

    private SortedMap<String, Integer> words = new TreeMap<String, Integer>();
    private int charCount;
    private int paraCount;
    private int paraSpeechCount;
    private int[] speechWords = new int[2];
    private int[] speechChars = new int[3];

    private boolean debugEnabled = false;
    private boolean freqEnabled = false;

    public void setDebugEnabled(boolean debugEnabled) {
        this.debugEnabled = debugEnabled;
    }

    public void setFreqEnabled(boolean freqEnabled) {
        this.freqEnabled = freqEnabled;
    }

    public boolean analyze(File file) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            try {
                analyze(in);
                showSpeechAnalysis();
            } finally {
                in.close();
            }
            return true;
        } catch (IOException ex) {
            System.err.println("Error reading from " + file);
            ex.printStackTrace();
            return false;
        }
    }

    public void analyze(String ins) {
        try {
            StringReader in = new StringReader(ins);
            try {
                analyze(in);
            } finally {
                in.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void analyze(Reader in) throws IOException {
        int k = '\0';
        int pk;
        StringBuilder token = new StringBuilder();
        boolean isSpeech = false;

        do {
            pk = k;
            k = in.read();
            if (Character.isAlphabetic(k) || k == 39 || k == 0x2018 || k == 0x2019) {
                //character is alphabetic or apostrophe / single quote
                if (Character.isLetter(k)) {
                    ++charCount;
                    token.append((char) Character.toUpperCase(k));
                }

            } else {
                //for all other characters, consider it a word boundary
                if ((k == 0x0A && pk == 0x0A) || (k == 0x0D && pk == 0x0D)) {
                    //new paragraph
                    if (isSpeech) {
                        ++paraSpeechCount;
                        if (debugEnabled) {
                            System.out.println("+");
                        }
                    }
                    ++paraCount;
                    isSpeech = false;
                } else if (k == 0x22) {
                    //indeterminate speech char
                    isSpeech = !isSpeech;
                    ++speechChars[0];
                } else if (k == 0x201C) {
                    //start speech char
                    isSpeech = true;
                    ++speechChars[1];
                } else if (k == 0x201D) {
                    //end speech char
                    isSpeech = false;
                    ++speechChars[2];
                }

                //deal with current word token
                String word = token.toString();
                if (word.length() >= MIN_WORD_LENGTH) {
                    Integer wordCount = words.get(word);
                    if (wordCount != null) {
                        words.put(word, wordCount + 1);
                    } else {
                        words.put(word, 1);
                    }
                    if (isSpeech) {
                        speechWords[1]++;
                        if (debugEnabled) {
                            System.out.println('*' + word);
                        }
                    } else {
                        speechWords[0]++;
                        if (debugEnabled) {
                            System.out.println(word);
                        }
                    }
                }
                token.setLength(0);

            }
        } while (k >= 0);

    }

    private void showSpeechAnalysis() {
        System.out.println();
        int totalChar = 0;
        //int totalDict = 0;
        for (String word : words.keySet()) {
            int freq = words.get(word);
            if (freqEnabled) {
                System.out.println(word + " : " + freq);
            }
            //totalDict += freq;
            totalChar += (freq * word.length());
        }

        System.out.println();
        System.out.println("Total Chars : " + charCount);
        System.out.println("Open Quotes : " + speechChars[1]);
        System.out.println("Close Quotes : " + speechChars[1]);
        System.out.println("Other Quotes : " + speechChars[0]);
        System.out.println();

        int totalWords = speechWords[0] + speechWords[1];
        System.out.println();
        System.out.println("Total words : " + totalWords);
        System.out.println("Prose Words : " + speechWords[0] + " = " + String.format("%1$.1f", (float)100*speechWords[0]/totalWords) + "%");
        System.out.println("Speech Words : " + speechWords[1] + " = " + String.format("%1$.1f", (float)100*speechWords[1]/totalWords) + "%");

        System.out.println();
        System.out.println("Total Paragraphs : " + paraCount);
        System.out.println("Paragraphs speech overflow: " + paraSpeechCount);

        System.out.println();
        //System.out.println("Dictionary size : " + totalDict);
        System.out.println("Total characters : " + totalChar);
        System.out.println("Average word size : " + String.format("%1$.2f", (float) totalChar / totalWords));
        System.out.println("Total unique words : " + words.size());
    }


    public static void main(String args[]) {
        if (args.length < 1) {
            System.out.println("SpeechAnalysis  [-d]  filename");
            System.exit(0);
        }

        boolean debugEnabled = false;
        boolean freqEnabled = false;
        for (int i=0; i<args.length; ++i) {
            if (args[i].equals("-d")) {
                debugEnabled = true;
            } else if (args[i].equals("-f")) {
                freqEnabled = true;
            }
        }

        String wordFile = args[args.length-1];

        SpeechAnalysis sa = new SpeechAnalysis();
        sa.setFreqEnabled(freqEnabled);
        sa.setDebugEnabled(debugEnabled);
        sa.analyze(new File(wordFile));
    }


}