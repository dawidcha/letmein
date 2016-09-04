package com.dawidcha.letmein.test.fixtures;

import com.dawidcha.letmein.data.controlmessage.BaseMessage;
import com.dawidcha.letmein.util.Fn;
import com.dawidcha.letmein.util.Holder;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.Json;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.LinkedList;

public class HttpClient extends Fixture {
    private final Vertx vertx;
    private int serverPort =  -1;
    private io.vertx.core.http.HttpClient client;
    private String accept;
    private String sessionId;

    public HttpClient(boolean showOutput) {
        super(showOutput);

        vertx = Vertx.vertx();
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public void start() {
        HttpClientOptions clientOpts = new HttpClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(serverPort);

        client = vertx.createHttpClient(clientOpts);
    }

    public void stop() {
        client.close();
        client = null;
    }

    public static class ReturnData {
        public final byte[] data;

        public ReturnData(byte[] data) {
            this.data = data;
        }
    }

    public ReturnData request(HttpMethod method, String uri, String data) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8.name());
            return request(method, uri, dataBytes);
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public ReturnData request(HttpMethod method, String uri, byte[] data) {
        String uriWithSessionId = (uri.contains("?")? (uri + "&"): (uri + "?")) + "sessionId=" + sessionId;
        HttpClientRequest req = client.request(method, uriWithSessionId);
        req.headers().add(HttpHeaders.ACCEPT, accept);

        final Holder<ReturnData> returnData = new Holder<>();
        req.handler(response -> response.bodyHandler(body -> {
            ReturnData ret = new ReturnData(body.getBytes());
            synchronized(returnData) {
                returnData.value = ret;
                returnData.notify();
            }
        }));

        if(data != null) {
            req.end(Buffer.buffer(data));
        }
        else {
            req.end();
        }

        synchronized(returnData) {
            while(returnData.value == null) {
                Fn.checkRun(returnData::wait);
            }
        }
        return returnData.value;
    }
}
