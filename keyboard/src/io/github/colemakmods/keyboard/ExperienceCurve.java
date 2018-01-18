package io.github.colemakmods.keyboard;

public class ExperienceCurve {

    private double pParam;

    public ExperienceCurve(double bParam) {
        this.pParam = Math.log(bParam)/Math.log(2);
        System.out.println();
        System.out.println(String.format("ExperienceCurve b=%f, p=%f", bParam, pParam));
    }

    public double getInstantCost(double freq) {
        return Math.pow(freq, pParam);
    }

    public double getIntegratedCost(double freq) {
        return Math.pow(freq, pParam+1)/(pParam+1);
    }

    public static void main(String[] args) {
        double b = 0.8;
        ExperienceCurve experienceCurve = new ExperienceCurve(b);
        for (double f = 0; f <=15; ++f) {
            System.out.println(String.format("%f :  %f  %f", f, experienceCurve.getInstantCost(f), experienceCurve.getIntegratedCost(f)));
        }
    }

}
