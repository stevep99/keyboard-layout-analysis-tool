package io.github.colemakmods.keyboard

import io.github.colemakmods.chars.Alphabet
import java.io.PrintStream

/**
 * Represents the keyboard layout being analyzed
 *
 * Created by steve on 18/10/14.
 */
class KeyboardLayout @JvmOverloads constructor(
    val name: String,
    val keyList: MutableList<Key> = mutableListOf(),
    var cols: Int = 0,
    var rows: Int = 0,
    var penaltySameFinger: DoubleArray = DoubleArray(3),
    var penaltyNeighbourFinger: Array<DoubleArray> = Array(4) { DoubleArray(3) },
    var keyboardType: KeyboardType = KeyboardType.STD,
) {

    enum class KeyboardType {
        STD,
        ANGLE,
        MATRIX_SIMPLE,
        MATRIX_ERGODOX,
    }

    fun duplicate(): KeyboardLayout {
        return KeyboardLayout(
            name, keyList, cols, rows, penaltySameFinger,
            penaltyNeighbourFinger, keyboardType
        )
    }

    fun duplicateWithName(nameName: String): KeyboardLayout {
        return KeyboardLayout(
            nameName, keyList, cols, rows, penaltySameFinger,
            penaltyNeighbourFinger, keyboardType
        )
    }

    fun addKey(row: Int, col: Int, chars: String?) {
        val key = Key(row, col, chars!!)
        keyList.add(key)
        if (cols < key.col + 1) {
            cols = key.col + 1
        }
        if (rows < key.row + 1) {
            rows = key.row + 1
        }
    }

    fun validate(): List<String> {
        val alphabet = generateAlphabet()
        val errors: MutableList<String> = ArrayList()
        errors.addAll(alphabet.findMissingLetters())
        errors.addAll(alphabet.findDuplicateLetters())
        return errors
    }

    fun lookupKey(c: Char): Key? {
        for (key in keyList) {
            if (key.hasChar(c)) {
                return key
            }
        }
        return null
    }

    fun lookupKey(r: Int, c: Int): Key? {
        for (key in keyList) {
            if (key.row == r && key.col == c) {
                return key
            }
        }
        return null
    }

    fun generateAlphabet(): Alphabet {
        val sb = StringBuilder()
        for (key in keyList) {
            sb.append(key.chars)
        }
        return Alphabet(sb.toString())
    }

    fun getPrimaryChars(): String {
        val sb = StringBuilder()
        for (key in keyList) {
            sb.append(key.getPrimaryChar())
        }
        return sb.toString()
    }

    fun getPenaltySameFinger(rowdiff: Int): Double {
        return penaltySameFinger[rowdiff]
    }

    fun setPenaltySameFinger(rowdiff: Int, penaltyAmount: Double) {
        penaltySameFinger[rowdiff] = penaltyAmount
    }

    fun getPenaltyNeighbourFinger(outermostFinger: Int, rowdiff: Int): Double {
        val outermostFingerLeft = if (outermostFinger <= 4) outermostFinger else 9 - outermostFinger
        return if (penaltyNeighbourFinger.size <= outermostFingerLeft) 0.0 else penaltyNeighbourFinger[outermostFingerLeft][rowdiff]
    }

    fun setPenaltyNeighbourFinger(outermostFinger: Int, rowdiff: Int, penaltyAmount: Double) {
        penaltyNeighbourFinger[outermostFinger][rowdiff] = penaltyAmount
    }

    fun hasPenaltyNeighbourFinger(outermostFinger: Int): Boolean {
        val outermostFingerLeft = if (outermostFinger <= 4) outermostFinger else 9 - outermostFinger
        if (penaltyNeighbourFinger.size <= outermostFingerLeft) return false
        for (r in 0..2) {
            if (penaltyNeighbourFinger[outermostFingerLeft][r] > 0) return true
        }
        return false
    }

    fun dumpLayout(out: PrintStream) {
        out.println()
        out.println("Keyboard '" + name + "' with " + keyList.size + " keys in " + rows + " rows:")
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val key = lookupKey(r, c)
                if (key != null) {
                    out.printf("%s ", key.getName())
                } else {
                    out.print(" ")
                }
            }
            out.println()
        }
        out.println("Alphabet ${generateAlphabet()}")
        out.println()
    }

    fun dumpConfig(out: PrintStream) {
        out.println("Effort")
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val key = lookupKey(r, c)
                if (key != null) {
                    out.printf(" % .1f ", key.effort)
                } else {
                    out.print("     ")
                }
            }
            out.println()
        }
        out.println("Fingers")
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val key = lookupKey(r, c)
                if (key != null) {
                    out.printf(" %s ", key.finger)
                } else {
                    out.print("   ")
                }
            }
            out.println()
        }
        out.println("Same-Finger Penalties")
        for (rowdiff in 0..2) {
            out.printf(" % .1f ", getPenaltySameFinger(rowdiff))
        }
        out.println()
        out.println("Neighbour-Finger Penalties")
        for (f in 0..2) {
            for (rowdiff in 0..2) {
                out.printf(" % .1f ", getPenaltyNeighbourFinger(f, rowdiff))
            }
            out.println()
        }
        out.println()
    }

}
