package com.dawidcha.letmein.handlers;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;

public class Control {

    public static Router makeRouter(Vertx vertx) {
        Router ret = Router.router(vertx);

        ret.route().handler(new ExtractSessionId());

        ret.put("/:device/:property").handler(context -> {

            context.response().end("OK for " + context.request().getParam("device") + "/" + context.request().getParam("property"));
        });

        return ret;
    }
}
