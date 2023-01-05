package io.github.colemakmods.sa;

import io.github.colemakmods.chars.BigramFreq;
import io.github.colemakmods.chars.CharFreq;
import io.github.colemakmods.keyboard.Key;
import io.github.colemakmods.keyboard.KeyboardAnalysis;
import io.github.colemakmods.keyboard.KeyboardLayout;
import io.github.colemakmods.keyboard.LayoutResults;

import java.util.List;

/**
 * Perform a simulated annealing to find an optimized layout based on an initial configuration
 * and with a specified Effort Model.
 */
public class SimulatedAnnealing {

    public static final Boolean SHOW_DEBUG_OUTPUT = false;

    public static KeyboardLayout runSimulation(KeyboardLayout keyboardLayout,
                                               List<CharFreq> charFreqs,
                                               List<BigramFreq> bigramFreqs,
                                               SASettings saSettings) {

        double coolingFactor = saSettings.alpha;
        double t = saSettings.highTemp;
        double costDelta = 0;
        double stateDelta = 0;
        KeyboardLayout current = keyboardLayout.duplicateWithName("sa-" + keyboardLayout.getName());
        double currentEffort = 0;
        double bestEffort = 0;
        double neighbourEffort = 0;
        KeyboardAnalysis ka = new KeyboardAnalysis();

//        int n = current.getKeyList().size();
//        Integer[] arr = IntStream.range(0, n).boxed().toArray(Integer[]::new);
//        List<Integer> intList = Arrays.asList(arr);
//        Collections.shuffle(intList);
//        intList.toArray(arr);
//
//        for (int i = 0; i < arr.length; i++) {
//            Key key = current.getKeyList().get(i);
//            key.setCol(arr[i] % current.getCols());
//            key.setRow(arr[i] / current.getCols());
//        }
//        current.dumpLayout(System.out);

        KeyboardLayout best = current.duplicate();

        LayoutResults layoutResults = ka.performAnalysis(current, charFreqs, bigramFreqs);
        currentEffort = saSettings.getEffortModel().getTotalEffort(layoutResults);
        bestEffort = currentEffort;

        // for (double t = temperature; t > 1; t *= coolingFactor) {
        while (t > 1e-3) {
            KeyboardLayout neighbour = current.duplicate();
            int key1_idx = (int) (neighbour.getKeyList().size() * Math.random());
            Key key1 = neighbour.getKeyList().get(key1_idx);

            int key2_idx = (int) (neighbour.getKeyList().size() * Math.random());
            Key key2 = neighbour.getKeyList().get(key2_idx);

            Key key1Copy = new Key(key1.getRow(), key1.getCol(), key1.getChars());

            key1.setChars(key2.getChars());

            key2.setChars(key1Copy.getChars());

            layoutResults = ka.performAnalysis(neighbour, charFreqs, bigramFreqs);
            neighbourEffort = saSettings.getEffortModel().getTotalEffort(layoutResults);
            double delta = coolingFactor * (neighbourEffort - currentEffort);
            boolean accepted = Math.random() < probability(delta, t);
            if (SHOW_DEBUG_OUTPUT) {
                System.out.println("Temp " + t + " costDelta " + costDelta + " stateDelta "
                    + stateDelta + " delta " + delta + " accepted " + accepted);
            }
            if (delta > 0) {
                stateDelta -= (delta / t);
            }
            if (accepted) {
                costDelta += delta;
                current = neighbour.duplicate();
                currentEffort = neighbourEffort;
            }
            t = calcTemp(costDelta, stateDelta, saSettings.highTemp, coolingFactor);

            if (currentEffort < bestEffort) {
                if (SHOW_DEBUG_OUTPUT) {
                    System.out.println("Best layout updated currentEffort " + currentEffort
                        + " bestEffort " + bestEffort + " temperature " + t);
                    System.out.println("Temp " + t + " costDelta " + costDelta + " stateDelta "
                        + stateDelta + " delta " + delta);
                }
                best = current.duplicate();
                bestEffort = currentEffort;
                best.dumpLayout(System.out);
            }

        }

        if (SHOW_DEBUG_OUTPUT) {
            System.out.println("Temp " + t + " costDelta " + costDelta + " stateDelta " + stateDelta);
        }
        return best;
    }

    private static double probability(double delta, double temp) {
        if (delta < 0)
            return 1;
        return Math.exp((-1 * delta) / temp);
    }

    private static double calcTemp(double costDelta, double stateDelta, double highTemp, double alpha) {
        if (costDelta >= 0 | stateDelta == 0) {
            return highTemp;
        }
        double newTemp = alpha * costDelta / stateDelta;
        if (newTemp < highTemp) {
            return newTemp;
        }
        return highTemp;
    }

}
