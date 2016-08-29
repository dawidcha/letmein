package com.dawidcha.letmein.handlers;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ApiRouters {
    private static final Logger log = LoggerFactory.getLogger(ApiRouters.class);

    public static void closeWithStatus(HttpServerResponse response, HttpResponseStatus status) {
        response.setStatusCode(status.code());
        response.setStatusMessage(status.reasonPhrase());
        response.end();
    }

    private static String decode(String input) {
        try {
            return URLDecoder.decode(input, StandardCharsets.UTF_8.name());
        }
        catch(UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static Map<String, String> getQueryParameters(HttpServerRequest req) {
        Map<String, String> ret = new HashMap<>();
        String[] pairs = req.query().split("&");
        for(String pair: pairs) {
            int eqIdx = pair.indexOf('=');
            if(eqIdx < 0) {
                ret.put(decode(pair), null);
            }
            else {
                ret.put(decode(pair.substring(0, eqIdx)), decode(pair.substring(eqIdx + 1)));
            }
        }
        return ret;
    }

    public static Router hubRouter(Vertx vertx) {
        Router ret = Router.router(vertx);

        ret.put("/:hubId").handler(rc -> {

        });

        return ret;
    }

}
