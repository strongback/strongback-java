package org.strongback.mock;

import org.strongback.components.RevSensor;

public class MockRevSensor implements RevSensor {
    private double revolutions;
    private double revolutionsPerMinute;

    protected MockRevSensor() {
        this(0.0, 0.0);
    }

    protected MockRevSensor(double revolutions, double revolutionsPerMinute) {
        this.revolutions = revolutions;
        this.revolutionsPerMinute = revolutionsPerMinute;
    }

    public void setRevolutions(double revolutions) {
        this.revolutions = revolutions;
    }

    public void setRevolutionsPerMinute(double revolutionsPerMinute) {
        this.revolutionsPerMinute = revolutionsPerMinute;
    }

    @Override
    public double getRevolutions() {
        return revolutions;
    }

    @Override
    public double getRevolutionsPerMinute() {
        return revolutionsPerMinute;
    }
}
