package com.dawidcha.letmein.test.fixtures;

import com.dawidcha.letmein.Launcher;
import com.dawidcha.letmein.data.controlmessage.BaseMessage;
import com.dawidcha.letmein.test.util.IOUtil;
import com.dawidcha.letmein.test.util.OutOfProcess;
import com.dawidcha.letmein.util.Fn;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class MockHostServer extends Fixture {
    private static int startupTimeoutMs = 60000;
    private final int serverPort;
    private OutOfProcess def;

    public MockHostServer(boolean showOutput) {
        super(showOutput);
        serverPort = IOUtil.findPort();
    }

    public int getServerPort() {
        return serverPort;
    }

    public OutOfProcess getProcessDef() {
        return def;
    }

    public void start() {
        Fn.check(this::startProcess);
    }

    public void stop() {
        Fn.check(this::stopProcess);
    }

    public void startProcess() throws IOException {
        List<String> cmd = new ArrayList<>();

        cmd.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        cmd.add("-classpath");
        cmd.add(System.getProperty("java.class.path"));
        cmd.add(Launcher.class.getName());
        cmd.add("-i");
        cmd.add("-p");
        cmd.add(Integer.toString(serverPort));

        String[] cmdArray = cmd.toArray(new String[cmd.size()]);

        def = new OutOfProcess(this, showOutput);

        System.out.println("Starting mock host server on port " + serverPort);
        final Process proc = Runtime.getRuntime().exec(cmdArray, new String[0], IOUtil.projectRoot);
        def.processStarted(proc);

        def.waitForMessages(startupTimeoutMs, new Pattern[]
                { Pattern.compile("\\QListening on " + serverPort + "\\E") }, false);

        System.out.println("Started");
    }

    public void stopProcess() throws Exception {
        def.killProcess(null);
    }
}
