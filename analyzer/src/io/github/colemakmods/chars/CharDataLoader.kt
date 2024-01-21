package io.github.colemakmods.chars

import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Reader
import java.io.StringReader

class CharDataLoader {

    fun initialize(alphabet: Alphabet): MutableList<CharFreq> {
        val charFreqList: MutableList<CharFreq> = ArrayList()
        for (i in 0 until alphabet.letters.length) {
            val c = alphabet.letters[i]
            charFreqList.add(CharFreq(c))
        }
        return charFreqList
    }

    fun initialize(alphabet: Alphabet, file: File): List<CharFreq>? {
        return try {
            read(alphabet, FileReader(file))
        } catch (ex: IOException) {
            System.err.println("Unable to load char frequency file")
            ex.printStackTrace()
            null
        }
    }

    fun initialize(alphabet: Alphabet, data: String): List<CharFreq>? {
        return try {
            read(alphabet, StringReader(data))
        } catch (ex: IOException) {
            System.err.println("Unable to load char frequency string")
            ex.printStackTrace()
            null
        }
    }

    @Throws(IOException::class)
    private fun read(alphabet: Alphabet, reader: Reader): List<CharFreq> {
        val charFreqList: MutableList<CharFreq> = ArrayList()
        BufferedReader(reader).use { br ->
            var line: String
            while (true) {
                line = br.readLine() ?: break
                val tokens = StringSplitter.split(line, ' ')
                if (!line.startsWith("#") && tokens.size >= 2) {
                    val token = tokens[0].uppercase()
                    if (token.length == 1) {
                        val ch = token.first()
                        if (alphabet.contains(ch)) {
                            val count = tokens[1].toLong()
                            charFreqList.add(CharFreq(ch, count))
                        }
                    }
                }
            }
        }
        CharFreq.normalize(charFreqList)
        //System.out.printf("Read %d char freq\n", cfreq.length);
        return charFreqList
    }

}