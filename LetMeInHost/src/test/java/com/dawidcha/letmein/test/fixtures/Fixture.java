package com.dawidcha.letmein.test.fixtures;

import com.dawidcha.letmein.util.Fn;

public abstract class Fixture implements AutoCloseable {
    protected final boolean showOutput;

    public Fixture(boolean showOutput) {
        this.showOutput = showOutput;
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public abstract void start();
    public abstract void stop();

    public void close() {
        Fn.check(this::stop);
    }
}
