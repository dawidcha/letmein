package com.dawidcha.letmein.test.fixtures;

import com.dawidcha.letmein.util.Fn;
import com.dawidcha.letmein.util.Holder;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

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
        public final String dataString;
        public final int statusCode;
        public final String statusMessage;

        public ReturnData(int statusCode, String statusMessage, byte[] data) {
            this.statusCode = statusCode;
            this.statusMessage = statusMessage;
            this.data = data;
            this.dataString = data == null? null: Fn.check(()-> new String(data, StandardCharsets.UTF_8.name()));
        }
    }

    public ReturnData request(HttpMethod method, String uri, MultiMap headers) {
        return request(method, uri, headers, (byte[])null);
    }

    public ReturnData request(HttpMethod method, String uri, MultiMap headers, String data) {
        try {
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8.name());
            return request(method, uri, headers, dataBytes);
        }
        catch(UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public ReturnData request(HttpMethod method, String uri, MultiMap headers, byte[] data) {
        String uriWithSessionId = (uri.contains("?")? (uri + "&"): (uri + "?")) + "sessionId=" + sessionId;
        HttpClientRequest req = client.request(method, uriWithSessionId);
        req.headers().add(HttpHeaders.ACCEPT, accept);
        if(headers != null) {
            req.headers().addAll(headers);
        }

        final Holder<ReturnData> returnData = new Holder<>();
        req.handler(response -> response.bodyHandler(body -> {
            ReturnData ret = new ReturnData(response.statusCode(), response.statusMessage(), body.getBytes());
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
