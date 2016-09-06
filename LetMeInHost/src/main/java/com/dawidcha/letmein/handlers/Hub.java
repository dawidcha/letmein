package com.dawidcha.letmein.handlers;

import com.dawidcha.letmein.Registry;
import com.dawidcha.letmein.data.HubInfo;
import com.dawidcha.letmein.data.controlmessage.ActionMessage;
import com.dawidcha.letmein.data.controlmessage.BaseMessage;
import com.dawidcha.letmein.data.controlmessage.ResponseMessage;
import com.dawidcha.letmein.data.controlmessage.StatusMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hub implements Handler<ServerWebSocket> {
    private static final Logger log = LoggerFactory.getLogger(Hub.class);
    private static final Pattern uriPattern = Pattern.compile("^/hub/([^/]+)$");
    private static final File local = new File("local").isDirectory()? new File("local"): new File("../local");

    private final Vertx vertx;
    private ServerWebSocket websocket;

    public Hub(Vertx vertx) {
        this.vertx = vertx;
    }

    private class MessageVisitor implements BaseMessage.Visitor {
        public void visit(ActionMessage action) {

        }

        public void visit(ResponseMessage response) {

        }

        public void visit(StatusMessage status) {

        }
    }

    private void setHandlers(ServerWebSocket websocket) {
        this.websocket = websocket;

        websocket.frameHandler(wsf -> {
            try {
                BaseMessage msg = Json.mapper.readValue(wsf.binaryData().getBytes(), BaseMessage.class);
                MessageVisitor visitor = new MessageVisitor();
                msg.accept(visitor);
            }
            catch(Exception e) {
                log.warn("Failed to parse message: " + new String(wsf.binaryData().getBytes()), e);
            }
        });

        websocket.closeHandler(Void -> Registry.closeHubConnection(websocket));
        websocket.exceptionHandler(e -> log.warn("Websocket threw exception", e));
    }

    @Override
    public void handle(ServerWebSocket serverWebSocket) {
        Matcher matcher = uriPattern.matcher(serverWebSocket.path());
        if(!matcher.find()) {
            log.info("Websocket connection to '" + serverWebSocket.path() + "' rejected because path was not recognized");
            serverWebSocket.reject();
        }
        else {
            String hubId = matcher.group(1);
            String filePath = local.getAbsolutePath() + "/hubs.json";
            vertx.fileSystem().readFile(filePath, result -> {
                if (result.succeeded()) {
                    // Check whether we're good to continue - it's too late to reject the websocket, but we can close it.
                    try {
                        final List<HubInfo> hubs;
                        hubs = Json.mapper.readValue(result.result().getBytes(), new TypeReference<List<HubInfo>>() {});
                        for(HubInfo hub: hubs) {
                            if (hub.id.equals(hubId)) {

                                // Acknowledge the connection by sending back the hub id
                                serverWebSocket.writeFinalTextFrame(Json.mapper.writeValueAsString(hubId));

                                Registry.registerHubConnection(hub, serverWebSocket);

                                setHandlers(serverWebSocket);
                                return;
                            }
                        }
                        log.info("No such hub id '" + hubId + "'");
                        serverWebSocket.close();
                    }
                    catch(Exception e) {
                        log.error("Failed to read booking info", e);
                        serverWebSocket.close();
                    }
                }
                else {
                    log.warn("Failed to read '" + filePath + "'", result.cause());
                    serverWebSocket.close();
                }
            });
        }
    }
}
