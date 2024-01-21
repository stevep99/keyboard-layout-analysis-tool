package io.github.colemakmods.keyboard

import io.github.colemakmods.chars.StringSplitter
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.Reader
import java.io.StringReader

object KeyboardMapping {
    fun initialize(keyboardLayout: KeyboardLayout, file: File): Boolean {
        return try {
            read(keyboardLayout, FileReader(file))
            true
        } catch (ex: Exception) {
            System.err.println("Unable to load layout file")
            ex.printStackTrace()
            false
        }
    }

    @JvmStatic
    fun initialize(keyboardLayout: KeyboardLayout, input: String): Boolean {
        return try {
            read(keyboardLayout, StringReader(input))
            true
        } catch (ex: Exception) {
            System.err.println("Unable to read layout data")
            ex.printStackTrace()
            false
        }
    }

    @Throws(IOException::class)
    private fun read(keyboardLayout: KeyboardLayout, reader: Reader) {
        BufferedReader(reader).use { br ->
            var line: String
            var row = 0
            while (true) {
                line = br.readLine() ?: break
                val commentpos = line.indexOf('#')
                if (commentpos >= 0) {
                    line = line.substring(0, commentpos)
                }
                line = line.trim { it <= ' ' }
                if (line.isEmpty()) continue
                val charList = StringSplitter.split(line, ' ')
                for (col in charList.indices) {
                    val chars = charList[col]
                    keyboardLayout.addKey(row, col, chars)
                }
                ++row
            }
        }
    }
}
