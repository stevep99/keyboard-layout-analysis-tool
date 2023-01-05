package io.github.colemakmods.sa;

import io.github.colemakmods.keyboard.LayoutResults;

public class SFBEffortModel implements EffortModel {
    @Override
    public double getTotalEffort(LayoutResults layoutResults) {
        return layoutResults.getTotalSFBEffort();
    }
}
