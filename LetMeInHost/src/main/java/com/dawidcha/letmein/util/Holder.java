package com.dawidcha.letmein.util;

public class Holder<T> {
    public T value;

    public Holder() {
        this(null);
    }

    public Holder(T value) {
        this.value = value;
    }
}
