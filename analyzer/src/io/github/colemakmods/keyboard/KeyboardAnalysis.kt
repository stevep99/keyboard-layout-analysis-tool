package io.github.colemakmods.keyboard

import io.github.colemakmods.chars.BigramDataLoader
import io.github.colemakmods.chars.BigramFreq
import io.github.colemakmods.chars.CharDataLoader
import io.github.colemakmods.chars.CharFreq
import io.github.colemakmods.chars.FreqAnalysis
import io.github.colemakmods.keyboard.report.KeyboardAnalysisBriefHTMLReport
import io.github.colemakmods.keyboard.report.KeyboardAnalysisTextReport
import io.github.colemakmods.sa.SASettings
import io.github.colemakmods.sa.SFBEffortModel
import io.github.colemakmods.sa.SimpleEffortModel
import io.github.colemakmods.sa.SimulatedAnnealing
import java.io.File
import java.io.IOException

/**
 * Perform an analysis on a keyboard layout using provided data on character and bigram frequencies,
 * penalties and finger configuration.
 *
 * Created by steve on 18/10/14.
 */
class KeyboardAnalysis {
    fun performAnalysis(
        keyboardLayout: KeyboardLayout,
        charFreqs: List<CharFreq>,
        bigramFreqs: List<BigramFreq>
    ): LayoutResults {
        val messages = sanityCheck(keyboardLayout)
        val keyFreq = calculateKeyFrequency(keyboardLayout, charFreqs)
        val fingerFreq = calculateFingerFrequency(keyboardLayout, charFreqs)
        val handAlternation = countHandAlternation(keyboardLayout, bigramFreqs)

        //find difficult bigrams for the current layout
        val sameFingerBigrams = findSameFingerBigrams(keyboardLayout, bigramFreqs)
        val neighbourPenaltyFingerBigrams =
            findNeighbourFingerBigrams(keyboardLayout, bigramFreqs, false)
        val neighbourBenefitFingerBigrams =
            findNeighbourFingerBigrams(keyboardLayout, bigramFreqs, true)
        val fingerEffort = arrayOf(
            calculateBaseEffort(keyboardLayout, charFreqs),
            calculateSameFingerBigramEffort(keyboardLayout, sameFingerBigrams),
            calculateNeighbourFingerBigramEffort(
                keyboardLayout,
                joinList(neighbourPenaltyFingerBigrams, neighbourBenefitFingerBigrams)
            )
        )
        return LayoutResults(
            keyboardLayout,
            messages,
            keyFreq,
            fingerFreq,
            handAlternation,
            sameFingerBigrams,
            neighbourPenaltyFingerBigrams,
            neighbourBenefitFingerBigrams,
            fingerEffort
        )
    }

    private fun joinList(list1: List<FingerBigram>, list2: List<FingerBigram>): List<FingerBigram> {
        val list: MutableList<FingerBigram> = ArrayList()
        list.addAll(list1)
        list.addAll(list2)
        return list
    }

    private fun sanityCheck(keyboardLayout: KeyboardLayout): List<String> {
        val messages: MutableList<String> = ArrayList()
        val alphabet = keyboardLayout.generateAlphabet()
        if (keyboardLayout.rows > 4) {
            messages.add("Warning: Too many rows")
        }
        messages.addAll(alphabet.findMissingLetters())
        messages.addAll(alphabet.findDuplicateLetters())
        return messages
    }

    private fun calculateKeyFrequency(
        keyboardLayout: KeyboardLayout,
        charFreqs: List<CharFreq>
    ): HashMap<Key, Double> {
        val keyFrequency = HashMap<Key, Double>()
        for (row in 0 until keyboardLayout.rows) {
            for (col in 0 until keyboardLayout.cols) {
                val key = keyboardLayout.lookupKey(row, col)
                if (key != null) {
                    var freqTotal = 0.0
                    for (ch in key.chars.toCharArray()) {
                        val cf = charFreqs.firstOrNull { ch == it.char }
                        if (cf != null) {
                            freqTotal += cf.freq.toDouble()
                        }
                    }
                    keyFrequency[key] = freqTotal
                }
            }
        }
        return keyFrequency
    }

    private fun calculateFingerFrequency(
        keyboardLayout: KeyboardLayout,
        charFreqs: List<CharFreq>
    ): DoubleArray {
        val fingerFreq = DoubleArray(10)
        for ((c, _, freq) in charFreqs) {
            val key = keyboardLayout.lookupKey(c)
            if (key != null) {
                fingerFreq[key.finger] += freq.toDouble()
            }
        }
        return fingerFreq
    }

    private fun countHandAlternation(
        keyboardLayout: KeyboardLayout,
        bigramFreqs: List<BigramFreq>
    ): Double {
        var handAlternation = 0.0
        for ((string, _, freq) in bigramFreqs) {
            val chars = string.toCharArray()
            val key1 = keyboardLayout.lookupKey(chars[0]) ?: continue
            val key2 = keyboardLayout.lookupKey(chars[1]) ?: continue
            if (key1.getHand() !== key2.getHand()) {
                handAlternation += freq.toDouble()
            }
        }
        return handAlternation
    }

    /**
     * Return a list of all same-finger bigrams for this layout
     *
     * @param keyboardLayout the current keyboard layout
     * @param bigramFreqs the full set of bigram frequencies
     * @return the list of same-finger bigrams found
     */
    private fun findSameFingerBigrams(
        keyboardLayout: KeyboardLayout,
        bigramFreqs: List<BigramFreq>
    ): List<FingerBigram> {
        val fingerBigrams = mutableListOf<FingerBigram>()
        for (bigramFreq in bigramFreqs) {
            val chars = bigramFreq.string.toCharArray()
            val key1 = keyboardLayout.lookupKey(chars[0])
            val key2 = keyboardLayout.lookupKey(chars[1])
            if (key1 != null && key2 != null) {
                if (chars[0] != chars[1] && KeyboardConfig.isSameFinger(key1.finger, key2.finger)) {
                    val fingerBigram = FingerBigram(key1, key2, bigramFreq)
                    fingerBigrams.add(fingerBigram)
                }
            }
        }
        return fingerBigrams
    }

    /**
     * Return a list of neighbour-finger bigrams for this layout that have defined penalties
     *
     * @param keyboardLayout the current keyboard layout
     * @param bigramFreqs the full set of bigram frequencies
     * @param benefit whether to get beneficial or detrimental bigrams
     * @return the list of neighbour-finger bigrams found
     */
    private fun findNeighbourFingerBigrams(
        keyboardLayout: KeyboardLayout,
        bigramFreqs: List<BigramFreq>,
        benefit: Boolean
    ): List<FingerBigram> {
        val fingerBigrams: MutableList<FingerBigram> = ArrayList()
        for (bigramFreq in bigramFreqs) {
            val chars = bigramFreq.string.toCharArray()
            val key1 = keyboardLayout.lookupKey(chars[0])
            val key2 = keyboardLayout.lookupKey(chars[1])
            if (key1 != null && key2 != null) {
                if (KeyboardConfig.isNeighbourFinger(key1.finger, key2.finger)) {
                    val outermostFinger =
                        KeyboardConfig.getOutermostFinger(key1.finger, key2.finger)
                    val fingerBigram = FingerBigram(key1, key2, bigramFreq)
                    if (keyboardLayout.hasPenaltyNeighbourFinger(outermostFinger) != benefit) {
                        fingerBigrams.add(fingerBigram)
                    }
                }
            }
        }
        return fingerBigrams
    }

    /**
     * Calculate the base effort incurred due to key position
     */
    private fun calculateBaseEffort(
        keyboardLayout: KeyboardLayout,
        charFreqs: List<CharFreq>
    ): DoubleArray {
        val baseEffort = DoubleArray(10)
        for (ch in keyboardLayout.generateAlphabet().asCharArray()) {
            val key = keyboardLayout.lookupKey(ch) ?: continue
            val cf = charFreqs.firstOrNull { ch == it.char }
            if (cf != null) {
                baseEffort[key.finger] += key.effort * cf.freq
            }
        }
        return baseEffort
    }

    /**
     * Calculate the effort incurred due to same-finger bigram penalties
     */
    private fun calculateSameFingerBigramEffort(
        keyboardLayout: KeyboardLayout,
        fingerBigrams: List<FingerBigram>
    ): DoubleArray {
        val bigramEffort = DoubleArray(10)
        for (fingerBigram in fingerBigrams) {
            val finger1 = fingerBigram.key1.finger
            val diffrows = Math.min(2, Math.abs(fingerBigram.key1.row - fingerBigram.key2.row))
            val penalty = keyboardLayout.getPenaltySameFinger(diffrows)
            bigramEffort[finger1] += fingerBigram.bigramFreq.freq * penalty
        }
        return bigramEffort
    }

    /**
     * Calculate the effort incurred due to neighbour-finger bigram penalties
     */
    private fun calculateNeighbourFingerBigramEffort(
        keyboardLayout: KeyboardLayout,
        fingerBigrams: List<FingerBigram>
    ): DoubleArray {
        val bigramEffort = DoubleArray(10)
        for (fingerBigram in fingerBigrams) {
            val finger1 = fingerBigram.key1.finger
            val finger2 = fingerBigram.key2.finger
            val outermostFinger = KeyboardConfig.getOutermostFinger(finger1, finger2)
            val diffrows = Math.min(2, Math.abs(fingerBigram.key1.row - fingerBigram.key2.row))
            val penalty = keyboardLayout.getPenaltyNeighbourFinger(outermostFinger, diffrows)
            bigramEffort[outermostFinger] += fingerBigram.bigramFreq.freq * penalty
        }
        return bigramEffort
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size < 3) {
                exitHelp()
                return
            }
            val options = Options()
            var saSettings: SASettings? = null
            var charFreqs: List<CharFreq>? = null
            var bigramFreqs: List<BigramFreq>? = null
            val keyboardFile = File(args[args.size - 1])
            var keyboardLayout = KeyboardLayout(keyboardFile.name)
            var valid = KeyboardMapping.initialize(keyboardLayout, keyboardFile)
            if (!valid) return
            keyboardLayout.dumpLayout(System.out)
            val alphabet = keyboardLayout.generateAlphabet()
            val charDataLoader = CharDataLoader()
            val bigramDataLoader = BigramDataLoader()
            var i = 0
            while (i < args.size - 1) {
                if (args[i] == "-c") {
                    val configFiles =
                        args[++i].split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (configFile in configFiles) {
                        valid = KeyboardConfig.initialize(keyboardLayout, File(configFile))
                        if (!valid) return
                    }
                    keyboardLayout.dumpConfig(System.out)
                } else if (args[i] == "-f") {
                    val frequencyFile = args[++i]
                    charFreqs = charDataLoader.initialize(alphabet, File(frequencyFile))
                    bigramFreqs = bigramDataLoader.initialize(alphabet, File(frequencyFile))
                    if (charFreqs == null || bigramFreqs == null) {
                        return
                    }
                } else if (args[i] == "-w") {
                    val wordFile = args[++i]
                    val fa = FreqAnalysis(alphabet, -1)
                    fa.analyze(File(wordFile))
                    charFreqs = fa.charFreqList
                    bigramFreqs = fa.bigramFreqList
                } else if (args[i] == "-b") {
                    try {
                        val bigramsListSizes =
                            args[++i].split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()
                        if (bigramsListSizes.size >= 1) {
                            options.sfbListSize = bigramsListSizes[0].toInt()
                        }
                        if (bigramsListSizes.size >= 2) {
                            options.nfbListSize = bigramsListSizes[1].toInt()
                        }
                    } catch (ex: NumberFormatException) {
                        System.err.println("Invalid argument: bigram list size (-b)")
                        return
                    }
                } else if (args[i].startsWith("-o")) {
                    options.outputFormat = args[++i]
                } else if (args[i].startsWith("--sa")) {
                    val effortModelClass = args[++i]
                    saSettings = when (effortModelClass) {
                        "SimpleEffortModel" -> SASettings(SimpleEffortModel())
                        "SFBEffortModel" -> SASettings(SFBEffortModel())
                        else -> {
                            System.err.println("Invalid argument: unknown --sa parameter")
                            return
                        }
                    }
                } else if (args[i].startsWith("-t")) {
                    if (saSettings != null) {
                        saSettings.highTemp = args[++i].toDouble()
                    }
                } else if (args[i].startsWith("-a")) {
                    if (saSettings != null) {
                        saSettings.alpha = args[++i].toDouble()
                    }
                }
                ++i
            }
            val errors = keyboardLayout.validate()
            if (!errors.isEmpty()) {
                for (error in errors) {
                    System.err.println("Error: $error")
                }
                return
            }
            if (charFreqs == null || bigramFreqs == null) {
                exitHelp()
                return
            }
            if (saSettings != null) {
                keyboardLayout = SimulatedAnnealing.runSimulation(
                    keyboardLayout,
                    charFreqs,
                    bigramFreqs,
                    saSettings
                )
            }
            val ka = KeyboardAnalysis()
            val layoutResults = ka.performAnalysis(keyboardLayout, charFreqs, bigramFreqs)
            try {
                // generate full text report
                if (options.outputFormat.indexOf('t') >= 0) {
                    val kr = KeyboardAnalysisTextReport(options.sfbListSize, options.nfbListSize)
                    val output = kr.generate(layoutResults)
                    println(output)
                }
                // generate brief report for html table
                if (options.outputFormat.indexOf('b') >= 0) {
                    val kr = KeyboardAnalysisBriefHTMLReport()
                    val output = kr.generate(layoutResults)
                    println(output)
                }
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
        }

        private fun exitHelp() {
            println("KeyboardAnalysis  -c configFiles..  -f frequencyFile  keyboardFile")
            println("KeyboardAnalysis  -c configFiles..  -w wordFile  keyboardFile")
            println("KeyboardAnalysis  -c configFiles..  -w wordFile  --sa effortModel  keyboardStartingFile")
            System.exit(0)
        }
    }
}
