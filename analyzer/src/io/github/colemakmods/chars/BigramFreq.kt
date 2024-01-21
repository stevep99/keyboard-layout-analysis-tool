package io.github.colemakmods.chars

import java.util.Comparator

/**
 * Represents a bigram (pair of consecutive letters) and the frequency with which that bigram occurs in a given corpus
 */
data class BigramFreq(val string: String,
                      var count: Long = 0L,
                      var freq: Float = 0f) {

    fun addCount() {
        count++
    }

    class BigramFreqComparer : Comparator<BigramFreq> {
        override fun compare(bf1: BigramFreq, bf2: BigramFreq): Int {
            return if (bf1.count > bf2.count) {
                -1
            } else if (bf1.count < bf2.count) {
                1
            } else {
                0
            }
        }
    }

    companion object {

        fun normalize(bigramFreqs: List<BigramFreq>) {
            var total: Long = 0
            for (bf in bigramFreqs) {
                total += bf.count
            }
            for (bf in bigramFreqs) {
                bf.freq = bf.count.toFloat() / total
            }
        }

    }
}
