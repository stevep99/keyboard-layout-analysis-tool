package io.github.colemakmods.chars

import io.github.colemakmods.chars.BigramFreq.BigramFreqComparer
import io.github.colemakmods.chars.CharFreq.CharFreqComparer
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.util.Collections
import kotlin.system.exitProcess

/**
 * Perform a frequency analysis on some input text (corpus), and generate as output the
 * character and bigram frequencies encountered.
 */
class FreqAnalysis(alphabet: Alphabet, maxUnicode: Int) {
    val charFreqList: MutableList<CharFreq>
    val bigramFreqList: List<BigramFreq>
    private val maxUnicode: Int

    init {
        charFreqList = CharDataLoader().initialize(alphabet)
        bigramFreqList = BigramDataLoader().initialize(alphabet)
        this.maxUnicode = if (maxUnicode > 0) {
            maxUnicode
        } else {
            DEFAULT_MAX_UNICODE
        }
    }

    fun analyze(file: File): Long {
        var i = -1L
        try {
            BufferedReader(FileReader(file)).use { br ->
                i = read(br)
            }
        } catch (ex: IOException) {
            System.err.println("Error reading from $file")
            ex.printStackTrace()
        }
        return i
    }

    fun analyze(ins: String): Long {
        var i = -1L
        try {
            StringReader(ins).use { sr ->
                i = read(sr)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return i
    }

    @Throws(IOException::class)
    private fun read(reader: Reader): Long {
        var k: Int
        var c = ' '
        var cp: Char
        var i = 0L

        //set up caches for faster reading
        val cCache = HashMap<Char, CharFreq>()
        for (cf in charFreqList) {
            cCache[cf.char] = cf
        }
        val bCache = HashMap<String, BigramFreq>()
        for (bf in bigramFreqList) {
            bCache[bf.string] = bf
        }
        while (true) {
            k = reader.read()
            if (k < 0) break
            cp = c
            c = k.toChar().uppercaseChar()
            if (k > 32) { //skip any control/non-printable characters
                var cf = cCache[c]
                if (cf != null) {
                    cf.addCount()
                } else if (k < maxUnicode && k != 65533) {
                    //for characters: add new item to list if new character is within requested unicode range
                    cf = CharFreq(k.toChar(), 1)
                    charFreqList.add(cf)
                    cCache[cf.char] = cf
                }
                val bigram = StringBuilder().append(cp).append(c).toString().uppercase()
                val bf = bCache[bigram]
                bf?.addCount()
            }
            ++i
            if (i % 10240 == 0L) {
                System.err.print("Read " + i / 1024 + " kb\r")
            }
        }
        System.err.print("Read " + i / 1024 + " kb (complete)\r")
        sort()
        return i
    }

    private fun sort() {
        Collections.sort(charFreqList, CharFreqComparer())
        Collections.sort(bigramFreqList, BigramFreqComparer())
    }

    private fun normalize() {
        CharFreq.normalize(charFreqList)
        BigramFreq.normalize(bigramFreqList)
    }

    fun showFrequencyAnalysis(showNormalized: Boolean) {
        for ((char, count, freq) in charFreqList) {
            if (showNormalized) {
                println("$char $count $freq")
            } else {
                println("$char $count")
            }
        }
        for ((string, count, freq) in bigramFreqList) {
            if (showNormalized) {
                println("$string $count $freq")
            } else {
                println("$string $count")
            }
        }
    }

    companion object {
        private val DEFAULT_ALPHABET = Alphabet("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        private const val DEFAULT_MAX_UNICODE = 0x10FFFF

        @JvmStatic
        fun main(args: Array<String>) {
            if (args.isEmpty()) {
                println("Usage:  FreqAnalysis  [-f]  [-a alphabet]  [-u max_unicode] filename")
                exitProcess(0)
            }
            val wordFile = args[args.size - 1]
            var alphabet = DEFAULT_ALPHABET
            var maxUnicode = -1
            var showNormalized = false

            var arg = 0
            while (arg < args.size - 1) {
                if (args[arg] == "-a") {
                    alphabet = Alphabet(args[++arg].uppercase())
                } else if (args[arg] == "-u") {
                    maxUnicode = args[++arg].toInt()
                } else if (args[arg] == "-f") {
                    showNormalized = true
                }
                ++arg
            }

            val timeStart = System.currentTimeMillis()
            val fa = FreqAnalysis(alphabet, maxUnicode)
            val i = fa.analyze(File(wordFile))
            if (showNormalized) {
                fa.normalize()
            }
            val timeEnd = System.currentTimeMillis()
            if (i > 0) {
                fa.showFrequencyAnalysis(showNormalized)
                System.err.println("Read " + i / 1024 + " kb in " + (timeEnd - timeStart) + "ms")
            }
        }
    }
}
