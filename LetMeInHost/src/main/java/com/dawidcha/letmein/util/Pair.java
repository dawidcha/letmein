package com.dawidcha.letmein.util;

import com.sun.istack.internal.NotNull;

public class Pair<A,B> implements Comparable<Pair<A,B>> {
    public final A first;
    public final B second;

    public Pair(A first, B second) {
        this.first = first;
        this.second = second;
    }

    private static boolean objsEqual(Object one, Object two) {
        return one == null? two == null: one.equals(two);
    }

    private static <A> int compareObjs(@NotNull A one, @NotNull A two) {
        //noinspection unchecked
        return one == null? two == null? 0: -1: two == null? 1: ((Comparable<A>)one).compareTo(two);
    }

    private static int objHash(Object o) {
        return o == null? 0: o.hashCode();
    }

    public String toString() {
        return "(" + first + "," + second + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if(obj.getClass() != Pair.class) {
            return false;
        }
        Pair other = (Pair)obj;
        return objsEqual(first, other.first) && objsEqual(second, other.second);
    }

    @Override
    public int hashCode() {
        return objHash(first) + objHash(second);
    }

    @Override
    public int compareTo(Pair<A, B> o) {
        int ret = compareObjs(first, o.first);
        if(ret == 0) {
            ret = compareObjs(second, o.second);
        }
        return ret;
    }
}
