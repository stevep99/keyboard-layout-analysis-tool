package io.github.colemakmods.chars

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Reader
import java.io.StringReader

class BigramDataLoader {
    fun initialize(alphabet: Alphabet): List<BigramFreq> {
        val bigramFreqList: MutableList<BigramFreq> = ArrayList()
        for (i in 0 until alphabet.letters.length) {
            for (j in 0 until alphabet.letters.length) {
                val str = StringBuilder().append(alphabet.letters[i]).append(alphabet.letters[j]).toString()
                bigramFreqList.add(BigramFreq(str))
            }
        }
        return bigramFreqList
    }

    fun initialize(alphabet: Alphabet, file: File): List<BigramFreq>? {
        return try {
            read(alphabet, FileReader(file))
        } catch (ex: IOException) {
            System.err.println("Unable to load bigram frequency file")
            ex.printStackTrace()
            null
        }
    }

    fun initialize(alphabet: Alphabet, data: String): List<BigramFreq>? {
        return try {
            read(alphabet, StringReader(data))
        } catch (ex: IOException) {
            System.err.println("Unable to load bigram frequency string")
            ex.printStackTrace()
            null
        }
    }

    @Throws(IOException::class)
    private fun read(alphabet: Alphabet, reader: Reader): List<BigramFreq> {
        val bigramFreqList: MutableList<BigramFreq> = ArrayList()
        BufferedReader(reader).use { br ->
            var line: String
            while (true) {
                line = br.readLine() ?: break
                val tokens = StringSplitter.split(line, ' ')
                if (!line.startsWith("#") && tokens.size >= 2) {
                    val bigram = tokens[0].uppercase()
                    if (bigram.length == 2) {
                        if (alphabet.letters.indexOf(bigram[0]) >= 0 && alphabet.letters.indexOf(bigram[1]) >= 0) {
                            val count = tokens[1].toLong()
                            bigramFreqList.add(BigramFreq(bigram, count))
                        }
                    }
                }
            }
        }
        BigramFreq.normalize(bigramFreqList)
        //System.out.printf("Read %d bigram freq\n", bfreq.length);
        return bigramFreqList
    }

}