package io.github.colemakmods.keyboard

import io.github.colemakmods.chars.BigramFreq

/**
 * Represents a Bigram of interest in the current analysis model
 * (currently same-finger and neighbour-finger bigrams)
 *
 * Created by steve on 10/05/15.
 */
class FingerBigram(val key1: Key,
                   val key2: Key,
                   val bigramFreq: BigramFreq) {

    companion object {
        /**
         * Get same-bigram frequencies per finger
         */
        fun getSameFingerBigramFreq(sameFingerBigrams: List<FingerBigram>): DoubleArray {
            val fingerBigramFreq = DoubleArray(10)
            for (fingerBigram in sameFingerBigrams) {
                val finger1 = fingerBigram.key1.finger
                val finger2 = fingerBigram.key2.finger
                if (KeyboardConfig.isSameFinger(finger1, finger2)) {
                    fingerBigramFreq[finger1] += fingerBigram.bigramFreq.freq.toDouble()
                }
            }
            return fingerBigramFreq
        }
    }
}
