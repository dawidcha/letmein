package com.dawidcha.letmein.test.util;

import com.dawidcha.letmein.util.Fn;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

public class IOUtil {
    private static int startPort = 14900 + (int)(System.currentTimeMillis() % 100);

    public static final File projectRoot;

    static {
        projectRoot = Fn.check(()->new File(".").getCanonicalFile());
    }

    public static boolean isPortUsed(int port) {
        try(ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return false;
        }
        catch(IOException e) {
            return true;
        }
    }

    public static int findPort(int startFrom) {
        if(!isPortUsed(startFrom)) {
            startPort = startFrom + 1;
            return startFrom;
        }
        return findPort(startFrom + 1);
    }

    public static int findPort() {
        return findPort(startPort);
    }
}
