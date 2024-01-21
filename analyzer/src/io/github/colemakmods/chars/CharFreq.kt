package io.github.colemakmods.chars

import java.util.Comparator
import kotlin.Char
import kotlin.Float
import kotlin.Int
import kotlin.Long

/**
 * Represents a single character and the frequency with which it occurs in a given corpus
 */
data class CharFreq(val char: Char,
                    var count: Long = 0L,
                    var freq: Float = 0f) {
    fun addCount() {
        count++
    }

    class CharFreqComparer : Comparator<CharFreq> {
        override fun compare(cf1: CharFreq, cf2: CharFreq): Int {
            return if (cf1.count > cf2.count) {
                -1
            } else if (cf1.count < cf2.count) {
                1
            } else {
                0
            }
        }
    }

    companion object {

        fun normalize(charFreqList: List<CharFreq>) {
            var total: Long = 0
            for (cf in charFreqList) {
                total += cf.count
            }
            for (cf in charFreqList) {
                cf.freq = cf.count.toFloat() / total
            }
        }
    }
}
