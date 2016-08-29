package com.dawidcha.letmein.test.fixtures;

import com.dawidcha.letmein.data.controlmessage.BaseMessage;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.WebSocket;
import io.vertx.core.json.Json;

import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;

public class MockHubAgent extends Fixture {
    private final Vertx vertx;
    private final Deque<byte[]> receivedMessages;
    private int serverPort =  -1;
    private HttpClient wsClient;
    private String hubId;
    private WebSocket ws;

    public MockHubAgent(boolean showOutput) {
        super(showOutput);

        vertx = Vertx.vertx();
        receivedMessages = new LinkedList<>();
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setHubId(String hubId) {
        this.hubId = hubId;
    }

    public byte[] popNextMessage() {
        return receivedMessages.pollFirst();
    }

    public void start() {
        HttpClientOptions clientOpts = new HttpClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(serverPort);

        wsClient = vertx.createHttpClient(clientOpts).websocket("/hub/" + hubId, websocket -> {
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
                    .exceptionHandler(e -> {
                        receivedMessages.add(e.getMessage().getBytes());
                    });

            synchronized(this) {
                ws = websocket;
                notify();
            }
        });

        try {
            synchronized (this) {
                while (ws == null) {
                    wait();
                }
            }
        }
        catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void stop() {
        synchronized(this) {
            ws.close();
            ws = null;
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
