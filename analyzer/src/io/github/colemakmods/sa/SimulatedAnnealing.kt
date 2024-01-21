package io.github.colemakmods.sa

import io.github.colemakmods.chars.BigramFreq
import io.github.colemakmods.chars.CharFreq
import io.github.colemakmods.keyboard.Key
import io.github.colemakmods.keyboard.KeyboardAnalysis
import io.github.colemakmods.keyboard.KeyboardLayout
import kotlin.math.exp

/**
 * Perform a simulated annealing to find an optimized layout based on an initial configuration
 * and with a specified Effort Model.
 */
object SimulatedAnnealing {
    const val SHOW_DEBUG_OUTPUT = false
    fun runSimulation(
        keyboardLayout: KeyboardLayout,
        charFreqs: List<CharFreq>,
        bigramFreqs: List<BigramFreq>,
        saSettings: SASettings,
    ): KeyboardLayout {

        val coolingFactor = saSettings.alpha
        var t = saSettings.highTemp
        var costDelta = 0.0
        var stateDelta = 0.0

        var current = keyboardLayout.duplicateWithName("sa-" + keyboardLayout.name)
        var currentEffort: Double
        var bestEffort: Double
        var neighbourEffort: Double
        val ka = KeyboardAnalysis()

//        current.dumpLayout(System.out);

        var best = current.duplicate()
        var layoutResults = ka.performAnalysis(current, charFreqs, bigramFreqs)
        currentEffort = saSettings.effortModel.getTotalEffort(layoutResults)
        bestEffort = currentEffort

        while (t > 1e-3) {
            val neighbour = current.duplicate()
            val key1_idx = (neighbour.keyList.size * Math.random()).toInt()
            val key1 = neighbour.keyList[key1_idx]
            val key2_idx = (neighbour.keyList.size * Math.random()).toInt()
            val key2 = neighbour.keyList[key2_idx]
            val key1Copy = Key(key1.row, key1.col, key1.chars)
            key1.chars = key2.chars
            key2.chars = key1Copy.chars
            layoutResults = ka.performAnalysis(neighbour, charFreqs, bigramFreqs)
            neighbourEffort = saSettings.effortModel.getTotalEffort(layoutResults)
            val delta = coolingFactor * (neighbourEffort - currentEffort)
            val accepted = Math.random() < probability(delta, t)
            if (SHOW_DEBUG_OUTPUT) {
                println(
                    "Temp " + t + " costDelta " + costDelta + " stateDelta "
                            + stateDelta + " delta " + delta + " accepted " + accepted
                )
            }
            if (delta > 0) {
                stateDelta -= delta / t
            }
            if (accepted) {
                costDelta += delta
                current = neighbour.duplicate()
                currentEffort = neighbourEffort
            }
            t = calcTemp(costDelta, stateDelta, saSettings.highTemp, coolingFactor)
            if (currentEffort < bestEffort) {
                if (SHOW_DEBUG_OUTPUT) {
                    println(
                        "Best layout updated currentEffort " + currentEffort
                                + " bestEffort " + bestEffort + " temperature " + t
                    )
                    println(
                        "Temp " + t + " costDelta " + costDelta + " stateDelta "
                                + stateDelta + " delta " + delta
                    )
                }
                best = current.duplicate()
                bestEffort = currentEffort
                best.dumpLayout(System.out)
            }
        }
        if (SHOW_DEBUG_OUTPUT) {
            println("Temp $t costDelta $costDelta stateDelta $stateDelta")
        }
        return best
    }

    private fun probability(delta: Double, temp: Double): Double {
        return if (delta < 0) 1.0 else exp(-1 * delta / temp)
    }

    private fun calcTemp(
        costDelta: Double,
        stateDelta: Double,
        highTemp: Double,
        alpha: Double
    ): Double {
        if ((costDelta >= 0) or (stateDelta == 0.0)) {
            return highTemp
        }
        val newTemp = alpha * costDelta / stateDelta
        return if (newTemp < highTemp) newTemp else highTemp
    }
}
