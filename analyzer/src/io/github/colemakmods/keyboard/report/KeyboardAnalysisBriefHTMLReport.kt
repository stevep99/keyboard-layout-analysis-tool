package io.github.colemakmods.keyboard.report

import io.github.colemakmods.keyboard.FingerBigram
import io.github.colemakmods.keyboard.LayoutResults
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream

/**
 * Generates a brief HTML summary report on the layout's results
 *
 * Created by steve on 09/05/15.
 */
class KeyboardAnalysisBriefHTMLReport : KeyboardAnalysisReport {

    @Throws(IOException::class)
    override fun generate(layoutResults: LayoutResults): String {
        val baos = ByteArrayOutputStream()
        val out = PrintStream(baos)
        showMessages(layoutResults.messages, out)
        showMain(layoutResults, out)
        return baos.toString("UTF-8")
    }

    private fun showMessages(messages: List<String>, out: PrintStream) {
        for (message in messages) {
            out.println(message)
        }
    }

    private fun showMain(layoutResults: LayoutResults, out: PrintStream) {
        val fingerBigramFreq = FingerBigram.getSameFingerBigramFreq(layoutResults.sameFingerBigrams)
        var bigramTotal = 0.0
        for (i in 0..4) {
            val right = 9 - i
            val leftFreq = fingerBigramFreq[i]
            val rightFreq = fingerBigramFreq[right]
            if (leftFreq > 0 || rightFreq > 0) {
                bigramTotal += leftFreq
                bigramTotal += rightFreq
            }
        }
        val fingerEffort = layoutResults.fingerEffort
        val fingerEffortTotal = DoubleArray(4)
        for (i in 0..9) {
            val allEffort = fingerEffort[0][i] + fingerEffort[1][i] + fingerEffort[2][i]
            if (fingerEffort[0][i] > 0) {
                fingerEffortTotal[0] += fingerEffort[0][i]
                fingerEffortTotal[1] += fingerEffort[1][i]
                fingerEffortTotal[2] += fingerEffort[2][i]
                fingerEffortTotal[3] += allEffort
            }
        }
        out.println()
        out.println("<tr>")
        out.printf(
            "<td>%s</td> <td>%s</td> <td>%.3f</td> <td>%.2f%%</td> <td>%.3f</td> <td>%.3f</td>\n",
            layoutResults.layout.name,
            layoutResults.layout.keyboardType,
            fingerEffortTotal[0],
            bigramTotal * 100,
            fingerEffortTotal[1] + fingerEffortTotal[2],
            fingerEffortTotal[3]
        )
        out.println("</tr>")
    }
}
