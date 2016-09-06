package com.dawidcha.letmein.handlers;

import com.dawidcha.letmein.Registry;
import com.dawidcha.letmein.data.BookingInfo;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;
import java.util.UUID;

public class ExtractSessionId implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext routingContext) {
        Map<String, String> queryParams = ApiRouters.getQueryParameters(routingContext.request());
        String sessionId = queryParams.get("sessionId");
        if(sessionId == null) {
            ApiRouters.closeWithStatus(routingContext.response(), HttpResponseStatus.UNAUTHORIZED);
            return;
        }

        routingContext.put("sessionId", sessionId);
        BookingInfo info = Registry.bookingInfoForSession(UUID.fromString(sessionId));
        if(info == null) {
            ApiRouters.closeWithStatus(routingContext.response(), HttpResponseStatus.FORBIDDEN);
            return;
        }

        routingContext.put("bookingInfo", info);
        routingContext.next();
    }
}
