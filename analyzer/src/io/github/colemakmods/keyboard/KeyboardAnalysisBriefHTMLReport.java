package io.github.colemakmods.keyboard;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import io.github.colemakmods.chars.BigramFreq;

/**
 * Generates a brief HTML summary report on the layout's results
 *
 * Created by steve on 09/05/15.
 */
public class KeyboardAnalysisBriefHTMLReport implements KeyboardAnalysisReport {

    @Override
    public String generate(LayoutResults layoutResults) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        showMessages(layoutResults.getMessages(), out);
        showMain(layoutResults, out);
        return baos.toString("UTF-8");
    }

    private void showMessages(List<String> messages, PrintStream out) {
        for (String message : messages) {
            out.println(message);
        }
    }

    private void showMain(LayoutResults layoutResults, PrintStream out) {
        double[] fingerBigramFreq = FingerBigram.getSameFingerBigramFreq(layoutResults.getSameFingerBigrams());

        double bigramTotal = 0;
        for (int i=0; i < 5; ++i) {
            int left = i;
            int right = 9-i;
            double leftFreq = fingerBigramFreq[left];
            double rightFreq = fingerBigramFreq[right];
            if (leftFreq > 0 || rightFreq > 0) {
                bigramTotal += leftFreq;
                bigramTotal += rightFreq;
            }
        }

        double[][] fingerEffort = layoutResults.getFingerEffort();
        double[] fingerEffortTotal = new double[4];
        for (int i = 0; i < 10; ++i) {
            double allEffort = fingerEffort[0][i] + fingerEffort[1][i] + fingerEffort[2][i];
            if (fingerEffort[0][i] > 0) {
                fingerEffortTotal[0] += fingerEffort[0][i];
                fingerEffortTotal[1] += fingerEffort[1][i];
                fingerEffortTotal[2] += fingerEffort[2][i];
                fingerEffortTotal[3] += allEffort;
            }
        }
        out.println();
        out.println("<tr>");
        out.printf("<td>%s</td> <td>%s</td> <td>%.3f</td> <td>%.2f%%</td> <td>%.3f</td> <td>%.3f</td>\n",
            layoutResults.getLayout().getName(),
            layoutResults.getLayout().getKeyboardType(),
            fingerEffortTotal[0],
            (bigramTotal * 100),
            (fingerEffortTotal[1] + fingerEffortTotal[2]),
            fingerEffortTotal[3]);
        out.println("</tr>");
    }

}
