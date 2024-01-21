package io.github.colemakmods.chars

data class Alphabet(val letters: String) {

    fun asCharArray(): CharArray {
        return letters.toCharArray()
    }

    fun contains(ch: Char) = letters.contains(ch)

    fun contains(str: String) = letters.contains(str)

    fun findMissingLetters(): List<String> {
        val messages = mutableListOf<String>()
        var ch = 'A'
        while (ch <= 'Z') {
            if (letters.indexOf(ch) < 0) {
                messages.add("Warning: letter $ch is missing from layout. ")
            }
            ++ch
        }
        return messages
    }

    fun findDuplicateLetters(): List<String> {
        val messages = mutableListOf<String>()
        for (i in letters.indices) {
            val c = letters[i]
            if (letters.indexOf(c, i + 1) >= 0) {
                messages.add("Warning: letter $c appears more than once on layout. ")
            }
        }
        return messages
    }

    override fun toString() = letters

}
