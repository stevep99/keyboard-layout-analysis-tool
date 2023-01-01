package io.github.colemakmods.sa;

public class SASettings {

    private EffortModel effortModel;
    public double highTemp = 128.0;
    public double alpha = 8.0;

    public SASettings(EffortModel effortModel) {
        this.effortModel = effortModel;
    }

    public EffortModel getEffortModel() {
        return effortModel;
    }
}
