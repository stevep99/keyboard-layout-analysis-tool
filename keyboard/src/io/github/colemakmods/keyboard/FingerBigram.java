package io.github.colemakmods.keyboard;

import java.util.List;

import io.github.colemakmods.chars.BigramFreq;

/**
 * Represents a Bigram of interest in the current analysis model
 * (currently same-finger and neighbour-finger bigrams)
 *
 * Created by steve on 10/05/15.
 */
public class FingerBigram {
    private Key key1;
    private Key key2;
    private BigramFreq bigramFreq;

    public FingerBigram(Key key1, Key key2, BigramFreq bigramFreq) {
        this.key1 = key1;
        this.key2 = key2;
        this.bigramFreq = bigramFreq;
    }

    public Key getKey1() {
        return key1;
    }

    public Key getKey2() {
        return key2;
    }

    public BigramFreq getBigramFreq() {
        return bigramFreq;
    }

    public boolean isSameFinger() {
        return FingerConfig.isSameFinger(key1.getFinger(), key2.getFinger());
    }

    public boolean isNeighbourFinger() {
        return FingerConfig.isNeighbourFinger(key1.getFinger(), key2.getFinger());
    }

    /**
     *  Get same-bigram frequencies per finger
     */
    public static double[] getSameFingerBigramFreq(List<FingerBigram> sameFingerBigrams) {
        double[] fingerBigramFreq = new double[10];
        for (FingerBigram fingerBigram : sameFingerBigrams) {
            int finger1 = fingerBigram.getKey1().getFinger();
            int finger2 = fingerBigram.getKey2().getFinger();
            if (FingerConfig.isSameFinger(finger1, finger2)) {
                fingerBigramFreq[finger1] += fingerBigram.getBigramFreq().getFreq();
            }
        }
        return fingerBigramFreq;
    }

}
