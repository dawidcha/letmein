package com.dawidcha.letmein.test.fixtures;

import com.dawidcha.letmein.data.controlmessage.BaseMessage;
import com.dawidcha.letmein.util.Fn;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class MockHubAgent extends Fixture {
    private final Vertx vertx;
    private final BlockingDeque<byte[]> receivedMessages;
    private int serverPort =  -1;
    private HttpClient wsClient;
    private String hubUri;
    private WebSocket ws;

    public MockHubAgent(boolean showOutput) {
        super(showOutput);

        vertx = Vertx.vertx();
        receivedMessages = new LinkedBlockingDeque<>();
    }

    public MockHubAgent setServerPort(int serverPort) {
        this.serverPort = serverPort;
        return this;
    }

    public MockHubAgent setHubId(String hubId) {
        this.hubUri = "/hub/" + hubId;
        return this;
    }

    public MockHubAgent setHubUri(String hubUri) {
        this.hubUri = hubUri;
        return this;
    }

    public byte[] popNextMessage() {
        return Fn.check(() -> receivedMessages.pollFirst(1, TimeUnit.SECONDS));
    }

    public void start() {
        HttpClientOptions clientOpts = new HttpClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(serverPort);

        wsClient = vertx.createHttpClient(clientOpts).websocket(hubUri, websocket -> {
            websocket
                    .frameHandler(frame -> {
                        byte[] messageBytes = frame.binaryData().getBytes();

                        if(showOutput) {
                            System.out.println("Message: " + new String(messageBytes));
                        }

                        byte[] copyOfMessage = new byte[messageBytes.length];
                        System.arraycopy(messageBytes, 0, copyOfMessage, 0, messageBytes.length);

                        receivedMessages.add(copyOfMessage);
                    })
                    .closeHandler(Void -> {
                        receivedMessages.add("Websocket closed by server".getBytes());
                        synchronized(this) {
                            ws = null;
                        }
                    })
                    .exceptionHandler(e -> receivedMessages.add(e.getMessage().getBytes()));

            synchronized(this) {
                ws = websocket;
                notify();
            }
        });

        try {
            synchronized (this) {
                // Failed connections do not report failure back to the client.  So we have to detect failure
                // to connect by virtue of a timeout (sigh).
                long stopAt = System.currentTimeMillis() + 1200;
                while (ws == null && System.currentTimeMillis() < stopAt) {
                    wait(100);
                }
            }
        }
        catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        synchronized(this) {
            if(ws != null) {
                ws.close();
                ws = null;
            }
        }
        wsClient.close();
        wsClient = null;
    }

    public void sendMessage(BaseMessage message) {
        try {
            ws.writeFinalBinaryFrame(Buffer.buffer(Json.mapper.writeValueAsBytes(message)));
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
