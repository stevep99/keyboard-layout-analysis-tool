package io.github.colemakmods.chars

import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Reader
import java.io.StringReader
import java.util.SortedMap
import java.util.TreeMap

/**
 * Created by steve on 19/01/17.
 */
class SpeechAnalysis {
    private val words: SortedMap<String, Int> = TreeMap()
    private var charCount = 0
    private var paraCount = 0
    private var paraSpeechCount = 0
    private val speechWords = IntArray(2)
    private val speechChars = IntArray(3)
    private var debugEnabled = false
    private var freqEnabled = false

    fun setDebugEnabled(debugEnabled: Boolean) {
        this.debugEnabled = debugEnabled
    }

    fun setFreqEnabled(freqEnabled: Boolean) {
        this.freqEnabled = freqEnabled
    }

    fun analyze(file: File): Boolean {
        return try {
            FileReader(file).use { br ->
                read(br)
                showSpeechAnalysis()
            }
            true
        } catch (ex: IOException) {
            System.err.println("Error reading from $file")
            ex.printStackTrace()
            false
        }
    }

    fun analyze(ins: String) {
        try {
            StringReader(ins).use { sr ->
                read(sr)
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }

    @Throws(IOException::class)
    private fun read(reader: Reader) {
        var k = '\u0000'.code
        var pk: Int
        val token = StringBuilder()
        var isSpeech = false
        while (true) {
            pk = k
            k = reader.read()
            if (k < 0) break
            if (Character.isAlphabetic(k) || k == 39 || k == 0x2018 || k == 0x2019) {
                //character is alphabetic or apostrophe / single quote
                if (Character.isLetter(k)) {
                    ++charCount
                    token.append(k.toChar().uppercaseChar())
                }
            } else {
                //for all other characters, consider it a word boundary
                if (k == 0x0A && pk == 0x0A || k == 0x0D && pk == 0x0D) {
                    //new paragraph
                    if (isSpeech) {
                        ++paraSpeechCount
                        if (debugEnabled) {
                            println("+")
                        }
                    }
                    ++paraCount
                    isSpeech = false
                } else if (k == 0x22) {
                    //indeterminate speech char
                    isSpeech = !isSpeech
                    ++speechChars[0]
                } else if (k == 0x201C) {
                    //start speech char
                    isSpeech = true
                    ++speechChars[1]
                } else if (k == 0x201D) {
                    //end speech char
                    isSpeech = false
                    ++speechChars[2]
                }

                //deal with current word token
                val word = token.toString()
                if (word.length >= MIN_WORD_LENGTH) {
                    val wordCount = words[word]
                    if (wordCount != null) {
                        words[word] = wordCount + 1
                    } else {
                        words[word] = 1
                    }
                    if (isSpeech) {
                        speechWords[1]++
                        if (debugEnabled) {
                            println("*$word")
                        }
                    } else {
                        speechWords[0]++
                        if (debugEnabled) {
                            println(word)
                        }
                    }
                }
                token.setLength(0)
            }
        }
    }

    private fun showSpeechAnalysis() {
        println()
        var totalChar = 0
        //int totalDict = 0;
        for (word in words.keys) {
            val freq = words[word]!!
            if (freqEnabled) {
                println("$word : $freq")
            }
            //totalDict += freq;
            totalChar += freq * word.length
        }
        println()
        println("Total Chars : $charCount")
        println("Open Quotes : " + speechChars[1])
        println("Close Quotes : " + speechChars[1])
        println("Other Quotes : " + speechChars[0])
        println()
        val totalWords = speechWords[0] + speechWords[1]
        println()
        println("Total words : $totalWords")
        println("Prose Words : " + speechWords[0] + " = " + String.format("%1$.1f", 100f * speechWords[0] / totalWords) + "%")
        println("Speech Words : " + speechWords[1] + " = " + String.format("%1$.1f", 100f * speechWords[1] / totalWords) + "%")
        println()
        println("Total Paragraphs : $paraCount")
        println("Paragraphs speech overflow: $paraSpeechCount")
        println()
        //System.out.println("Dictionary size : " + totalDict);
        println("Total characters : $totalChar")
        println("Average word size : " + String.format("%1$.2f", totalChar.toFloat() / totalWords))
        println("Total unique words : " + words.size)
    }

    companion object {
        private const val MIN_WORD_LENGTH = 1
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 1) {
                println("SpeechAnalysis  [-d]  filename")
                System.exit(0)
            }
            var debugEnabled = false
            var freqEnabled = false
            for (i in args.indices) {
                if (args[i] == "-d") {
                    debugEnabled = true
                } else if (args[i] == "-f") {
                    freqEnabled = true
                }
            }
            val wordFile = args[args.size - 1]
            val sa = SpeechAnalysis()
            sa.setFreqEnabled(freqEnabled)
            sa.setDebugEnabled(debugEnabled)
            sa.analyze(File(wordFile))
        }
    }
}