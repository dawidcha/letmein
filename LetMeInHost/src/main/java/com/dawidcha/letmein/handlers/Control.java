package com.dawidcha.letmein.handlers;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class Control {

    public static Router makeRouter(Vertx vertx) {
        Router ret = Router.router(vertx);

        ret.route().handler(new ExtractSessionId());

        ret.get("/status").handler(rc -> {

        });

        return ret;
    }
}
