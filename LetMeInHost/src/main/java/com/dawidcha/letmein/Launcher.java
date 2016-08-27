package com.dawidcha.letmein;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class Launcher {
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        VertxOptions vo = new VertxOptions()
                .setMaxEventLoopExecuteTime(15000);

        Vertx vertx = Vertx.vertx(vo);

        Router router = Router.router(vertx);
        router.mountSubRouter("/hub", ApiRouters.hubRouter(vertx));
        router.mountSubRouter("/login", ApiRouters.loginRouter(vertx));
        router.mountSubRouter("/control", ApiRouters.controlRouter(vertx));
        router.route("/site/*").handler(StaticHandler.create().setWebRoot("web"));

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080, result -> {
                    if(result.succeeded()) {
                        log.info("Listening on 8080");
                    }
                    else {
                        log.error("Failed");
                    }
                });
    }
}
