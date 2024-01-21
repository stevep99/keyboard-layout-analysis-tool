package io.github.colemakmods.keyboard

import io.github.colemakmods.chars.CharDataLoader
import io.github.colemakmods.chars.CharFreq
import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.PrintStream
import java.util.Properties

/**
 * Perform a comparison of a keyboard layout for differences against a base layout (usually Qwerty)
 *
 * Created by steve on 28/11/14.
 */
class KeyboardCompare {
    fun performCompare(
        keyboardLayout1: KeyboardLayout,
        keyboardLayout2: KeyboardLayout,
        charFreqs: List<CharFreq>,
        posOverrides: Properties?,
        out: PrintStream
    ) {
        val experienceCurve = ExperienceCurve(EXP_B_PARAM)
        var totalPosDiff = 0.0
        var totalFreq = 0.0
        var totalScore = 0.0
        val count = IntArray(6)
        out.println()
        for (ch in keyboardLayout1.getPrimaryChars().uppercase().toCharArray()) {
            val key1 = keyboardLayout1.lookupKey(ch)
            val key2 = keyboardLayout2.lookupKey(ch)
            var posdiff: Double
            val valOverride =
                if (posOverrides != null) posOverrides[ch.toString()] as String? else null
            if (valOverride != null) {
                posdiff = valOverride.toDouble()
                count[5]++
            } else if (key1 != null && key2 != null) {
                if (key1.col == key2.col && key1.row == key2.row) {
                    posdiff = 0.0
                    count[0]++
                } else if (key1.finger == key2.finger) {
                    posdiff = 0.25
                    count[1]++
                } else if (key1.getHand() === key2.getHand()) {
                    posdiff = 0.5
                    count[2]++
                } else if (key1.finger + key2.finger == 9) {
                    posdiff = 0.75
                    count[3]++
                } else {
                    posdiff = 1.0
                    count[4]++
                }
            } else {
                posdiff = 1.0
                count[4]++
            }
            totalPosDiff += posdiff
            var effortRatio = 1.0
            if (key1 != null && key2 != null) {
                effortRatio = key2.effort / key1.effort
            }
            val cf = charFreqs.firstOrNull { ch == it.char }
            if (cf != null) {
                val freqCost = experienceCurve.getIntegratedCost((100 * cf.freq).toDouble())
                val score = posdiff * freqCost * effortRatio
                out.printf(
                    "%s:  %f  %f  %f  %f  %f\n",
                    ch,
                    posdiff,
                    cf.freq,
                    freqCost,
                    effortRatio,
                    score
                )
                totalFreq += cf.freq.toDouble()
                totalScore += score
            }
        }
        out.println()
        out.printf("*: %f  %f  %f\n", totalPosDiff, totalFreq, totalScore)
        out.println()
        val totalMoved = count[1] + count[2] + count[3] + count[4]
        out.printf("Keys moved : %d\n", totalMoved)
        out.printf("  Unchanged: %d\n\n", count[0])
        out.printf(" SH / SF : %d\n", count[1])
        out.printf(" SH / DF : %d\n", count[2])
        out.printf(" DH / SF : %d\n", count[3])
        out.printf(" DH / DF : %d\n", count[4])
        if (posOverrides != null) {
            out.printf("Override: %d\n", count[5])
        }
    }

    companion object {
        private const val EXP_B_PARAM = 0.8
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 3) {
                exitHelp()
                return
            }
            val keyboard1Filename = args[args.size - 4]
            val keyboardLayout1 = KeyboardLayout(keyboard1Filename)
            val ok1 = KeyboardMapping.initialize(keyboardLayout1, File(keyboard1Filename))
            if (!ok1) return

            val config1Filename = args[args.size - 3]
            KeyboardConfig.initialize(keyboardLayout1, File(config1Filename))
            keyboardLayout1.dumpLayout(System.out)
            keyboardLayout1.dumpConfig(System.out)
            val keyboard2Filename = args[args.size - 2]
            val keyboardLayout2 = KeyboardLayout(keyboard2Filename)
            val ok2 = KeyboardMapping.initialize(keyboardLayout2, File(keyboard2Filename))
            if (!ok2) return

            val config2Filename = args[args.size - 1]
            KeyboardConfig.initialize(keyboardLayout2, File(config2Filename))
            keyboardLayout2.dumpLayout(System.out)
            keyboardLayout2.dumpConfig(System.out)
            var charFreqs: List<CharFreq>? = null
            var posOverrides: Properties? = null
            for (i in 0 until args.size - 1) {
                if (args[i] == "-f") {
                    val frequencyFile = args[i + 1]
                    charFreqs = CharDataLoader().initialize(
                        keyboardLayout1.generateAlphabet(), File(frequencyFile)
                    )
                } else if (args[i] == "-p") {
                    val posFile = args[i + 1]
                    posOverrides = Properties()
                    try {
                        posOverrides.load(FileReader(posFile))
                    } catch (ex: IOException) {
                        System.err.println("Unable to load position-override file")
                        ex.printStackTrace()
                        return
                    }
                }
            }
            if (charFreqs == null) {
                return
            }
            val kc = KeyboardCompare()
            kc.performCompare(keyboardLayout1, keyboardLayout2, charFreqs, posOverrides, System.out)
        }

        private fun exitHelp() {
            println("KeyboardCompare  -f frequencyFile  [-p positionOverrideFile]  keyboard1File  config1File  keyboard2File  config2File")
            System.exit(0)
        }
    }
}
