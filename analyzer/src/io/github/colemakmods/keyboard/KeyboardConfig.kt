package io.github.colemakmods.keyboard

import io.github.colemakmods.chars.StringSplitter
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Reader
import java.io.StringReader

/**
 * The type of finger configuration used for typing. For definitions, see
 * http://colemakmods.github.io/mod-dh/analyze.html
 *
 * Created by steve on 20/10/14.
 */
object KeyboardConfig {

    enum class Section {
        FINGERS,
        EFFORT,
        PENALTIES,
        TYPE
    }

    @JvmStatic
    fun initialize(keyboardLayout: KeyboardLayout, input: String): Boolean {
        return try {
            read(keyboardLayout, StringReader(input))
            true
        } catch (ex: IOException) {
            System.err.println("Unable to load finger key definitions file")
            ex.printStackTrace()
            false
        }
    }

    fun initialize(keyboardLayout: KeyboardLayout, file: File): Boolean {
        return try {
            read(keyboardLayout, FileReader(file))
            true
        } catch (ex: IOException) {
            System.err.println("Unable to load finger key definitions file")
            ex.printStackTrace()
            false
        }
    }

    @Throws(IOException::class)
    private fun read(keyboardLayout: KeyboardLayout, reader: Reader) {
        var section = Section.FINGERS
        var sectionRows = 3
        var line: String
        BufferedReader(reader).use { br ->
            var row = 0
            while (true) {
                line = br.readLine() ?: break
                val commentpos = line.indexOf('#')
                if (commentpos >= 0) {
                    line = line.substring(0, commentpos)
                }
                line = line.trim()
                if (line.isEmpty()) continue
                if (line.contains(":")) {
                    val tokens = StringSplitter.split(line, ':')
                    try {
                        section = Section.valueOf(tokens[0].uppercase())
                        sectionRows = if (tokens.size > 1) {
                            tokens[1].trim { it <= ' ' }.toInt()
                        } else {
                            3
                        }
                        row = 0
                    } catch (e: Exception) {
                        System.err.println("Invalid section " + tokens[0])
                    }
                    continue
                }
                //handle sections
                when (section) {
                    Section.EFFORT -> {
                        val tokens = StringSplitter.split(line, ' ')
                        for (col in tokens.indices) {
                            val keyboardRow = row + (keyboardLayout.rows - sectionRows)
                            if (keyboardRow >= 0) {
                                val key = keyboardLayout.lookupKey(keyboardRow, col)
                                if (key != null) {
                                    key.effort = tokens[col].toDouble()
                                }
                            }
                        }
                        ++row
                    }

                    Section.FINGERS -> {
                        val tokens = StringSplitter.split(line, ' ')
                        for (col in tokens.indices) {
                            val keyboardRow = row + (keyboardLayout.rows - sectionRows)
                            if (keyboardRow >= 0) {
                                val key = keyboardLayout.lookupKey(keyboardRow, col)
                                if (key != null) {
                                    key.finger = tokens[col].toInt()
                                }
                            }
                        }
                        ++row
                    }

                    Section.PENALTIES -> {
                        val tokens = StringSplitter.split(line, ' ')
                        if (row == 0) {
                            //same-finger penalty
                            for (rowdiff in tokens.indices) {
                                keyboardLayout.setPenaltySameFinger(
                                    rowdiff,
                                    tokens[rowdiff].toDouble()
                                )
                            }
                        } else {
                            //neighbour-finger penalty
                            val outermostFinger = row - 1
                            for (rowdiff in tokens.indices) {
                                keyboardLayout.setPenaltyNeighbourFinger(
                                    outermostFinger,
                                    rowdiff,
                                    tokens[rowdiff].toDouble()
                                )
                            }
                        }
                        ++row
                    }

                    Section.TYPE -> {
                        val tokens = StringSplitter.split(line, ' ')
                        try {
                            keyboardLayout.keyboardType =
                                KeyboardLayout.KeyboardType.valueOf(tokens[0].uppercase())
                        } catch (ex: Exception) {
                            throw IOException("Invalid KeyboardType " + tokens[0])
                        }
                        ++row
                    }
                }
            }
        }
    }

    fun isSameFinger(f1: Int, f2: Int) = (f1 == f2)

    fun isNeighbourFinger(f1: Int, f2: Int) = (f1 - f2 == 1 || f1 - f2 == -1)

    fun getOutermostFinger(f1: Int, f2: Int): Int {
        val left1 = if (f1 <= 4) f1 else 9 - f1
        val left2 = if (f2 <= 4) f2 else 9 - f2
        return if (left1 < left2) f1 else f2
    }

}
