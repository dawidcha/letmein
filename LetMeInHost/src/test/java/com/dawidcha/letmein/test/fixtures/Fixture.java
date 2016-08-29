package com.dawidcha.letmein.test.fixtures;

public abstract class Fixture {
    protected final boolean showOutput;

    public Fixture(boolean showOutput) {
        this.showOutput = showOutput;
    }

    public abstract void start() throws Exception;
    public abstract void stop() throws Exception;
}
