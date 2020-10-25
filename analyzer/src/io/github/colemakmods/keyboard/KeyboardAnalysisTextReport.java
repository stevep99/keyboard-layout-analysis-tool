package io.github.colemakmods.keyboard;

import java.io.*;
import java.util.List;

/**
 * Generates a formatted, full text report on the layout's results
 *
 * Created by steve on 18/10/14.
 */
public class KeyboardAnalysisTextReport implements KeyboardAnalysisReport {

    private int maxSameFingerBigrams;
    private int maxNeighbourFingerBigrams;

    public KeyboardAnalysisTextReport(int maxSameFingerBigrams, int maxNeighbourFingerBigrams) {
        this.maxSameFingerBigrams = maxSameFingerBigrams;
        this.maxNeighbourFingerBigrams = maxNeighbourFingerBigrams;
    }

    @Override
    public String generate(LayoutResults layoutResults) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        show(layoutResults, out);
        return baos.toString("UTF-8");
    }

    private void show(LayoutResults layoutResults, PrintStream out) {
        showMessages(layoutResults.getMessages(), out);
        showFingerFreq(layoutResults.getFingerFreq(), out);
        showBigramStats(layoutResults.getHandAlternation(), out);
        showFingerBigramFreq(layoutResults.getSameFingerBigrams(), out);
        showSameFingerBigrams(layoutResults.getSameFingerBigrams(), maxSameFingerBigrams, out);
        showNeighbourFingerBigrams(layoutResults.getNeighbourFingerBigrams(), maxNeighbourFingerBigrams, out);
        //showFingerEffortSimple(layoutResults.getFingerEffort(), out);
        showFingerEffortDetailed(layoutResults.getFingerEffort(), out);
    }

    private void showMessages(List<String> messages, PrintStream out) {
        for (String message : messages) {
            out.println(message);
        }
    }

    private void showFingerFreq(double[] fingerFreq, PrintStream out) {
        out.println();
        out.println("== Finger Frequency ==");
        double totalLeft = 0;
        double totalRight = 0;
        for (int i=0; i < 5; ++i) {
            int left = i;
            int right = 9-i;
            if (fingerFreq[left] > 0 || fingerFreq[right] > 0) {
                out.printf("finger %d:  %f      finger %d:  %f\n", left, fingerFreq[left], right, fingerFreq[right]);
            }
            totalLeft += fingerFreq[left];
            totalRight += fingerFreq[right];
        }
        out.printf(" total L:  %f       total R:  %f\n", totalLeft, totalRight);
    }

    private void showBigramStats(double handAlternation, PrintStream out) {
        out.println();
        out.println("== Bigram stats ==");
        out.printf("Hand Alternation: %f\n", handAlternation);
    }

    private void showFingerBigramFreq(List<FingerBigram> sameFingerBigrams, PrintStream out) {
        double[] fingerBigramFreq = FingerBigram.getSameFingerBigramFreq(sameFingerBigrams);

        out.println();
        out.println("== Finger Bigram Frequency ==");
        double total = 0;
        for (int i=0; i < 5; ++i) {
            int left = i;
            int right = 9-i;
            double leftFreq = fingerBigramFreq[left];
            double rightFreq = fingerBigramFreq[right];
            if (leftFreq > 0 || rightFreq > 0) {
                out.printf("finger %d:  %f      finger %d:  %f\n", left, leftFreq, right, rightFreq);
                total += leftFreq;
                total += rightFreq;
            }
        }
        out.printf("        total *:  %f\n", total);
    }

    private void showSameFingerBigrams(List<FingerBigram> sameFingerBigrams, int maxFingerBigrams, PrintStream out) {
        out.println();
        out.println("== Top Same-Finger Bigrams ==");
        int count = 0;
        for (FingerBigram fingerBigram : sameFingerBigrams) {
            out.printf("finger %d:  %s  %f\n", fingerBigram.getKey1().getFinger(),
                fingerBigram.getBigramFreq().getString(), fingerBigram.getBigramFreq().getFreq());
            if (++count >= maxFingerBigrams) {
                return;
            }
        }
    }

    private void showNeighbourFingerBigrams(List<FingerBigram> neighbourFingerBigrams, int maxFingerBigrams, PrintStream out) {
        out.println();
        out.println("== Top Neighbour-Finger Bigrams ==");
        int count = 0;
        for (FingerBigram fingerBigram : neighbourFingerBigrams) {
            out.printf("fingers %d-%d:  %s  %f\n", fingerBigram.getKey1().getFinger(), fingerBigram.getKey2().getFinger(),
                fingerBigram.getBigramFreq().getString(), fingerBigram.getBigramFreq().getFreq());
            if (++count >= maxFingerBigrams) {
                return;
            }
        }
    }

    private void showFingerEffortSimple(double[][] fingerEffort, PrintStream out) {
        out.println();
        out.println("== Finger Effort ==");
        out.println("               base   bigrams     total");
        double[] fingerEffortTotal = new double[3];
        for (int i = 0; i < 10; ++i) {
            double bigramEffort = fingerEffort[1][i] + fingerEffort[2][i];
            double allEffort = fingerEffort[0][i] + bigramEffort;
            if (fingerEffort[0][i] > 0) {
                out.printf("finger %d:  %f  %f  %f\n", i, fingerEffort[0][i], bigramEffort, allEffort);
                fingerEffortTotal[0] += fingerEffort[0][i];
                fingerEffortTotal[1] += fingerEffort[1][i];
                fingerEffortTotal[2] += fingerEffort[2][i];
            }
        }
        double bigramEffortTotal = fingerEffortTotal[1] + fingerEffortTotal[2];
        double allEffortTotal = fingerEffortTotal[0] + bigramEffortTotal;
        out.printf(" total *:  %f  %f  %f\n", fingerEffortTotal[0], bigramEffortTotal, allEffortTotal);
    }

    private void showFingerEffortDetailed(double[][] fingerEffort, PrintStream out) {
        out.println();
        out.println("== Finger Effort ==");
        out.println("               base  s-bigram  n-bigram     total");
        double[] fingerEffortTotal = new double[3];
        for (int i = 0; i < 10; ++i) {
            double allEffort = fingerEffort[0][i] + fingerEffort[1][i] + fingerEffort[2][i];
            if (fingerEffort[0][i] > 0) {
                out.printf("finger %d:  %f  %f  %f  %f\n", i, fingerEffort[0][i], fingerEffort[1][i], fingerEffort[2][i], allEffort);
                fingerEffortTotal[0] += fingerEffort[0][i];
                fingerEffortTotal[1] += fingerEffort[1][i];
                fingerEffortTotal[2] += fingerEffort[2][i];
            }
        }
        double allEffortTotal = fingerEffortTotal[0] + fingerEffortTotal[1] + fingerEffortTotal[2];
        out.printf(" total *:  %f  %f  %f  %f\n", fingerEffortTotal[0], fingerEffortTotal[1], fingerEffortTotal[2], allEffortTotal);
    }

}
