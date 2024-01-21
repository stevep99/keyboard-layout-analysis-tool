package io.github.colemakmods.keyboard

/**
 * Created by steve on 18/10/14.
 */
class Key(var row: Int,
          var col: Int,
          inputChars: String) {

    var finger: Int
    var effort = 0.0
    var chars: String = ""
        set(value) {
            if (value.length == 1) {
                val shifted = generateDefaultShifted(value[0])
                if (shifted != null) {
                    field = "${value[0]}${shifted}"
                } else {
                    field = value
                }
            } else {
                field = value
            }
        }

    init {
        this.chars = inputChars
        finger = getDefaultFinger()
    }

    fun hasChar(ch: Char): Boolean {
        for (c in chars.toCharArray()) {
            if (c == ch.uppercaseChar()) return true
        }
        return false
    }

    fun getName() = chars.first().uppercaseChar()

    fun getPrimaryChar() = chars.first()

    fun getHand() = if (finger <= 4) Hand.LEFT else Hand.RIGHT

    fun duplicate(): Key {
        val k = Key(row, col, chars)
        k.finger = finger
        k.effort = effort
        return k
    }

    fun getDefaultFinger(): Int {
        return if (col <= 3) {
            col
        } else if (col == 4) {
            3
        } else if (col == 5) {
            6
        } else if (col <= 9) {
            col
        } else {
            9
        }
    }

    companion object {
        private fun generateDefaultShifted(ch: Char): Char? {
            return if (Character.isAlphabetic(ch.code)) {
                if (Character.isLowerCase(ch)) {
                    ch.uppercaseChar()
                } else {
                    ch.lowercaseChar()
                }
            } else when (ch) {
                '1' -> '!'
                '2' -> '@'
                '3' -> '#'
                '4' -> '$'
                '5' -> '%'
                '6' -> '^'
                '7' -> '&'
                '8' -> '*'
                '9' -> '('
                '0' -> ')'
                '\'' -> '\"'
                '-' -> '_'
                '=' -> '+'
                '[' -> '{'
                ']' -> '}'
                ';' -> ':'
                ',' -> '<'
                '.' -> '>'
                '/' -> '?'
                else -> null
            }
        }
    }
}
