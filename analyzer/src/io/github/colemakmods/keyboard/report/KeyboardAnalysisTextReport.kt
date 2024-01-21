package io.github.colemakmods.keyboard.report

import io.github.colemakmods.keyboard.FingerBigram
import io.github.colemakmods.keyboard.LayoutResults
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream

/**
 * Generates a formatted, full text report on the layout's results
 *
 * Created by steve on 18/10/14.
 */
class KeyboardAnalysisTextReport(
    private val maxSameFingerBigrams: Int,
    private val maxNeighbourFingerBigrams: Int
) : KeyboardAnalysisReport {

    @Throws(IOException::class)
    override fun generate(layoutResults: LayoutResults): String {
        val baos = ByteArrayOutputStream()
        val out = PrintStream(baos)
        show(layoutResults, out)
        return baos.toString("UTF-8")
    }

    private fun show(layoutResults: LayoutResults?, out: PrintStream) {
        showMessages(layoutResults!!.messages, out)
        showFingerFreq(layoutResults.fingerFreq, out)
        showBigramStats(layoutResults.handAlternation, out)
        showFingerBigramFreq(layoutResults.sameFingerBigrams, out)
        showSameFingerBigrams(layoutResults.sameFingerBigrams, maxSameFingerBigrams, out)
        showNeighbourFingerBigrams(
            layoutResults.neighbourPenaltyFingerBigrams,
            maxNeighbourFingerBigrams,
            out
        )
        //showFingerEffortSimple(layoutResults.getFingerEffort(), out);
        showFingerEffortDetailed(layoutResults.fingerEffort, out)
    }

    private fun showMessages(messages: List<String>, out: PrintStream) {
        for (message in messages) {
            out.println(message)
        }
    }

    private fun showFingerFreq(fingerFreq: DoubleArray, out: PrintStream) {
        out.println()
        out.println("== Finger Frequency ==")
        var totalLeft = 0.0
        var totalRight = 0.0
        for (i in 0..4) {
            val right = 9 - i
            if (fingerFreq[i] > 0 || fingerFreq[right] > 0) {
                out.printf(
                    "finger %d:  %f      finger %d:  %f\n",
                    i,
                    fingerFreq[i],
                    right,
                    fingerFreq[right]
                )
            }
            totalLeft += fingerFreq[i]
            totalRight += fingerFreq[right]
        }
        out.printf(" total L:  %f       total R:  %f\n", totalLeft, totalRight)
    }

    private fun showBigramStats(handAlternation: Double, out: PrintStream) {
        out.println()
        out.println("== Bigram stats ==")
        out.printf("Hand Alternation: %f\n", handAlternation)
    }

    private fun showFingerBigramFreq(sameFingerBigrams: List<FingerBigram>, out: PrintStream) {
        val fingerBigramFreq = FingerBigram.getSameFingerBigramFreq(sameFingerBigrams)
        out.println()
        out.println("== Finger Bigram Frequency ==")
        var total = 0.0
        for (i in 0..4) {
            val right = 9 - i
            val leftFreq = fingerBigramFreq[i]
            val rightFreq = fingerBigramFreq[right]
            if (leftFreq > 0 || rightFreq > 0) {
                out.printf("finger %d:  %f      finger %d:  %f\n", i, leftFreq, right, rightFreq)
                total += leftFreq
                total += rightFreq
            }
        }
        out.printf("        total *:  %f\n", total)
    }

    private fun showSameFingerBigrams(
        sameFingerBigrams: List<FingerBigram>,
        maxFingerBigrams: Int,
        out: PrintStream
    ) {
        out.println()
        out.println("== Top Same-Finger Bigrams ==")
        for ((count, fingerBigram) in sameFingerBigrams.withIndex()) {
            out.printf(
                "finger %d:  %s  %f\n", fingerBigram.key1.finger,
                fingerBigram.bigramFreq.string, fingerBigram.bigramFreq.freq
            )
            if (count + 1 >= maxFingerBigrams) {
                return
            }
        }
    }

    private fun showNeighbourFingerBigrams(
        neighbourFingerBigrams: List<FingerBigram>,
        maxFingerBigrams: Int,
        out: PrintStream
    ) {
        out.println()
        out.println("== Top Neighbour-Finger Bigrams ==")
        var count = 0
        for (fingerBigram in neighbourFingerBigrams) {
            out.printf(
                "fingers %d-%d:  %s  %f\n", fingerBigram.key1.finger, fingerBigram.key2.finger,
                fingerBigram.bigramFreq.string, fingerBigram.bigramFreq.freq
            )
            if (++count >= maxFingerBigrams) {
                return
            }
        }
    }

    private fun showFingerEffortSimple(fingerEffort: Array<DoubleArray>, out: PrintStream) {
        out.println()
        out.println("== Finger Effort ==")
        out.println("              base  bigrams    total")
        val fingerEffortTotal = DoubleArray(3)
        for (i in 0..9) {
            val bigramEffort = fingerEffort[1][i] + fingerEffort[2][i]
            val allEffort = fingerEffort[0][i] + bigramEffort
            if (fingerEffort[0][i] > 0) {
                out.printf(
                    "finger %d:   % .3f   % .3f   % .3f\n",
                    i,
                    fingerEffort[0][i],
                    bigramEffort,
                    allEffort
                )
                fingerEffortTotal[0] += fingerEffort[0][i]
                fingerEffortTotal[1] += fingerEffort[1][i]
                fingerEffortTotal[2] += fingerEffort[2][i]
            }
        }
        val bigramEffortTotal = fingerEffortTotal[1] + fingerEffortTotal[2]
        val allEffortTotal = fingerEffortTotal[0] + bigramEffortTotal
        out.printf(
            " total *:   % .3f   % .3f   % .3f\n",
            fingerEffortTotal[0],
            bigramEffortTotal,
            allEffortTotal
        )
    }

    private fun showFingerEffortDetailed(fingerEffort: Array<DoubleArray>, out: PrintStream) {
        out.println()
        out.println("== Finger Effort ==")
        out.println("              base s-bigram n-bigram    total")
        val fingerEffortTotal = DoubleArray(3)
        for (i in 0..9) {
            val allEffort = fingerEffort[0][i] + fingerEffort[1][i] + fingerEffort[2][i]
            if (fingerEffort[0][i] > 0) {
                out.printf(
                    "finger %d:   % .3f   % .3f   % .3f   % .3f\n",
                    i,
                    fingerEffort[0][i],
                    fingerEffort[1][i],
                    fingerEffort[2][i],
                    allEffort
                )
                fingerEffortTotal[0] += fingerEffort[0][i]
                fingerEffortTotal[1] += fingerEffort[1][i]
                fingerEffortTotal[2] += fingerEffort[2][i]
            }
        }
        val allEffortTotal = fingerEffortTotal[0] + fingerEffortTotal[1] + fingerEffortTotal[2]
        out.printf(
            " total *:   % .3f   % .3f   % .3f   % .3f\n",
            fingerEffortTotal[0],
            fingerEffortTotal[1],
            fingerEffortTotal[2],
            allEffortTotal
        )
    }
}
