package io.github.colemakmods.web.report;

import io.github.colemakmods.keyboard.FingerBigram;
import io.github.colemakmods.keyboard.report.KeyboardAnalysisReport;
import io.github.colemakmods.keyboard.LayoutResults;
import io.github.colemakmods.web.teavm.JSFormatter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

public class KeyboardAnalysisWebHTMLReport implements KeyboardAnalysisReport {

    private int maxFingerBigrams;

    public KeyboardAnalysisWebHTMLReport(int maxFingerBigrams) {
        this.maxFingerBigrams = maxFingerBigrams;
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
        showFingerBigramFreq(layoutResults.getSameFingerBigrams(), out);
        out.print("<div style=\"display: table-row\">");
        out.print("<div style=\"display: table-cell\">");
        showSameFingerBigrams(layoutResults.getSameFingerBigrams(), maxFingerBigrams, out);
        out.print("</div>");
        out.print("<div style=\"display: table-cell;padding-left:2ex\">");
        showNeighbourFingerBigrams(layoutResults.getNeighbourPenaltyFingerBigrams(), maxFingerBigrams, out);
        out.print("</div>");
        out.print("</div>");
        showFingerEffort(layoutResults.getFingerEffort(), out);
    }

    private void showMessages(List<String> messages, PrintStream out) {
        if (messages != null) {
            for (String message : messages) {
                out.println("<b style=\"color:red\">" + message + "</b>");
            }
        }
    }

    private void showFingerFreq(double[] fingerFreq, PrintStream out) {
        out.println("<b><u>Finger Frequency</u></b>");
        out.println("<table>");
        double totalLeft = 0;
        double totalRight = 0;
        for (int i=0; i < 5; ++i) {
            int left = i;
            int right = 9-i;
            if (fingerFreq[left] > 0 || fingerFreq[right] > 0) {
                out.println("<tr>");
                out.println("<td>finger " + left + " </td><td>" + format(fingerFreq[left]*100, 2)
                        + "%</td><td>&nbsp;finger " + right + " </td><td>" + format(fingerFreq[right]*100, 2) + "%</td>");
                out.println("</tr>");
            }
            totalLeft += fingerFreq[left];
            totalRight += fingerFreq[right];
        }
        out.println("<tr class=\"row_total\">");
        out.println("<td class=\"center\">total L </td><td>" + format(totalLeft*100, 2)
                + "%</td><td class=\"center\">total R </td><td>" + format(totalRight*100, 2) + "%</td>");
        out.println("</tr>");
        out.println("</table>");
    }

    private void showFingerBigramFreq(List<FingerBigram> sameFingerBigrams, PrintStream out) {
        double[] fingerBigramFreq = FingerBigram.Companion.getSameFingerBigramFreq(sameFingerBigrams);

        out.println("<b><u>Finger Bigram Frequency</u></b>");
        out.println("<table>");
        double total = 0;
        for (int i=0; i < 5; ++i) {
            int left = i;
            int right = 9-i;
            if (fingerBigramFreq[left] > 0 || fingerBigramFreq[right] > 0) {
                out.println("<tr>");
                out.println("<td>finger " + left + " </td><td>" + format(fingerBigramFreq[left]*100, 3)
                        + "%</td><td>&nbsp;finger " + right + " </td><td>" + format(fingerBigramFreq[right]*100, 3) + "%</td>");
                out.println("</tr>");
                total += fingerBigramFreq[left];
                total += fingerBigramFreq[right];
            }
        }
        out.println("<tr class=\"row_total\">");
        out.println("<td class=\"center\" colspan=\"2\" style=\"text-align:right\">total</td><td class=\"center\" colspan=\"2\">"
                + format(total*100,3)  + "%</td>");
        out.println("</tr>");
        out.println("</table>");
    }

    private void showSameFingerBigrams(List<FingerBigram> sameFingerBigrams, int maxFingerBigrams, PrintStream out) {
        out.println("<b><u>Top Same-Finger Bigrams</u></b>");
        out.println("<table>");
        int count = 0;
        for (FingerBigram fingerBigram : sameFingerBigrams) {
            out.println("<tr>");
            out.println("<td>finger " + fingerBigram.getKey1().getFinger() + " </td><td>"
                    + fingerBigram.getBigramFreq().getString() + " </td><td>"
                    + format(fingerBigram.getBigramFreq().getFreq()*100, 3) + "%</td>");

            out.println("</tr>");
            if (++count >= maxFingerBigrams) {
                break;
            }
        }
        out.println("</table>");
    }

    private void showNeighbourFingerBigrams(List<FingerBigram> neighbourFingerBigrams, int maxFingerBigrams, PrintStream out) {
        out.println("<b><u>Top Neighbour-Finger Bigrams</u></b>");
        out.println("<table>");
        int count = 0;
        for (FingerBigram fingerBigram : neighbourFingerBigrams) {
            out.println("<tr>");
            out.println("<td>finger " + fingerBigram.getKey1().getFinger() + "-"
                + fingerBigram.getKey2().getFinger() + " </td><td>"
                + fingerBigram.getBigramFreq().getString() + " </td><td>"
                + format(fingerBigram.getBigramFreq().getFreq()*100, 3) + "%</td>");

            out.println("</tr>");
            if (++count >= maxFingerBigrams) {
                break;
            }
        }
        out.println("</table>");
    }

    private void showFingerEffort(double[][] fingerEffort, PrintStream out) {
        out.println("<b><u>Finger Effort</u></b>");
        out.println("<table>");
        out.println("<tr>");
        out.println("<th> </th><th>base</th><th>s-bigrams</th><th>n-bigrams</th><th>total</th>");
        out.println("</tr>");
        double[] fingerEffortTotal = new double[3];
        for (int i = 0; i < 10; ++i) {
            double allEffort = fingerEffort[0][i] + fingerEffort[1][i] + fingerEffort[2][i];
            if (fingerEffort[0][i] > 0) {
                out.println("<tr>");

                out.println("<td>finger "
                    + i + " </td><td>"
                    + format(fingerEffort[0][i], 3)
                    + "</td><td>"
                    + format(fingerEffort[1][i],3)
                    + "</td><td>"
                    + ((i <= 2 || i >= 7) ? format(fingerEffort[2][i],3) : "")
                    + "</td><td>"
                    + format(allEffort,3) + "</td>");
                out.println("</tr>");
                fingerEffortTotal[0] += fingerEffort[0][i];
                fingerEffortTotal[1] += fingerEffort[1][i];
                fingerEffortTotal[2] += fingerEffort[2][i];
            }
        }
        double bigramEffortTotal = fingerEffortTotal[1] + fingerEffortTotal[2];
        double allEffortTotal = fingerEffortTotal[0] + bigramEffortTotal;
        out.println("<tr class=\"row_total\">");
        out.println("<td>total * </td><td>" + format(fingerEffortTotal[0], 3) + "</td><td> "
            + format(fingerEffortTotal[1], 3) + "</td><td> "
            + format(fingerEffortTotal[2], 3) + "</td><td> "
            + format(allEffortTotal,3) + "</td>");
        out.println("</tr>");
        out.println("</table>");
    }

    private static String format(double value, int dp) {
        return JSFormatter.toFixed((float)value, dp);
    }

    private static String format(float value, int dp) {
        return JSFormatter.toFixed(value, dp);
    }

}
