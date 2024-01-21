package io.github.colemakmods.keyboard

import kotlin.math.ln
import kotlin.math.pow

class ExperienceCurve(bParam: Double) {
    private val pParam: Double

    init {
        pParam = ln(bParam) / ln(2.0)
        println()
        println(String.format("ExperienceCurve b=%f, p=%f", bParam, pParam))
    }

    fun getInstantCost(freq: Double): Double {
        return freq.pow(pParam)
    }

    fun getIntegratedCost(freq: Double): Double {
        return freq.pow(pParam + 1) / (pParam + 1)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val b = 0.8
            val experienceCurve = ExperienceCurve(b)
            for (f in 0..15) {
                val instantCost = experienceCurve.getInstantCost(f.toDouble())
                val integratedCost = experienceCurve.getIntegratedCost(f.toDouble())
                println("$f :  $instantCost  $integratedCost")
            }
        }
    }
}
