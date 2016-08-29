package com.dawidcha.letmein.test.fixtures;

import com.dawidcha.letmein.Launcher;
import com.dawidcha.letmein.data.controlmessage.BaseMessage;
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

public class MockHostServer extends Fixture {
    private final int serverPort;
    Process proc;

    public MockHostServer(boolean showOutput) {
        super(showOutput);

        serverPort = 8080;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void start() throws IOException {
        List<String> cmd = new ArrayList<>();

        cmd.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        cmd.add("--classpath");
        cmd.add(System.getProperty("java.class.path"));
        cmd.add(Launcher.class.getName());
        cmd.add("-i");
        cmd.add("-p");
        cmd.add(Integer.toString(serverPort));

        String[] cmdArray = cmd.toArray(new String[cmd.size()]);

        proc = Runtime.getRuntime().exec(cmdArray);
    }

    public void stop() {
        proc.destroy();
        proc = null;
    }
}
