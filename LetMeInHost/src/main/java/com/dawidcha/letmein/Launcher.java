package com.dawidcha.letmein;

import com.dawidcha.letmein.handlers.ApiRouters;
import com.dawidcha.letmein.handlers.Control;
import com.dawidcha.letmein.handlers.Hub;
import com.dawidcha.letmein.handlers.Login;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.commons.cli.*;

public class Launcher {
    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        Options options = new Options();

        options.addOption("i", "insecure", false, "Use insecure communication (http/ws instead of https/wss)");
        options.addOption("p", "port", true, "Port number to listen on (8080)");

        CommandLineParser cmdParser = new DefaultParser();
        try {
            CommandLine cmd = cmdParser.parse(options, args);
            boolean insecure = cmd.hasOption('i');
            int port = Integer.parseInt(cmd.getOptionValue('p', "8080"));

            VertxOptions vo = new VertxOptions()
                    .setMaxEventLoopExecuteTime(15000);

            Vertx vertx = Vertx.vertx(vo);

            Router router = Router.router(vertx);
            router.mountSubRouter("/hub", ApiRouters.hubRouter(vertx));
            router.mountSubRouter("/login", Login.makeRouter(vertx));
            router.mountSubRouter("/control", Control.makeRouter(vertx));
            router.route("/site/*").handler(StaticHandler.create().setWebRoot("web"));

            HttpServerOptions serverOptions = new HttpServerOptions();

            if(!insecure) {
                serverOptions.setSsl(true);
            }

            vertx.createHttpServer(serverOptions)
                    .requestHandler(router::accept)
                    .websocketHandler(new Hub(vertx))
                    .listen(port, result -> {
                        if (result.succeeded()) {
                            log.info("Listening on " + port);
                        } else {
                            log.error("Failed");
                        }
                    });
        }
        catch(Exception e) {
            log.error("Unexpected exception: ", e);

            HelpFormatter fmt = new HelpFormatter();
            fmt.printHelp(Launcher.class.getName(), "The following options are supported", options, "", true);
        }
    }
}
