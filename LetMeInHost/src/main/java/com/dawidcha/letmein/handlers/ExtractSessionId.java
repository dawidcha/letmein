package com.dawidcha.letmein.handlers;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

public class ExtractSessionId implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext routingContext) {
        Map<String, String> queryParams = ApiRouters.getQueryParameters(routingContext.request());
        String sessionId = queryParams.get("sessionId");

        routingContext.put("sessionId", sessionId);

        routingContext.next();
    }
}
