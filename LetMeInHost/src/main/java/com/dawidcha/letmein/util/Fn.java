package com.dawidcha.letmein.util;

import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.jar.Pack200;

public class Fn {
    public interface ThrowingRunnable {
        void run() throws Throwable;
    }

    public interface ThrowingSupplier<T> {
        T get() throws Throwable;
    }

    public static <T> T check(ThrowingSupplier<T> op) {
        try {
            return op.get();
        }
        catch(Throwable e) {
            if(e instanceof Error) {
                throw (Error)e;
            }
            else if(e instanceof RuntimeException) {
                throw (RuntimeException)e;
            }
            else if(e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException(e);
        }
    }

    public static void check(ThrowingRunnable op) {
        check(()->{
            op.run();
            return null;
        });
    }

    public static void checkRun(ThrowingRunnable op) {
        check(op);
    }
}
