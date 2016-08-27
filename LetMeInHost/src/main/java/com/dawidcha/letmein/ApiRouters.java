package com.dawidcha.letmein;

import com.dawidcha.letmein.data.BookingInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class ApiRouters {
    private static final Logger log = LoggerFactory.getLogger(ApiRouters.class);

    private static void closeWithStatus(HttpServerResponse response, HttpResponseStatus status) {
        response.setStatusCode(status.code());
        response.setStatusMessage(status.reasonPhrase());
        response.end();
    }

    public static Router hubRouter(Vertx vertx) {
        Router ret = Router.router(vertx);

        ret.put("/:hubId").handler(rc -> {

        });

        return ret;
    }

    public static Router controlRouter(Vertx vertx) {
        Router ret = Router.router(vertx);

        ret.put("/").handler(rc -> {

        });

        return ret;
    }

    public static Router loginRouter(Vertx vertx) {
        Router ret = Router.router(vertx);

        ret.get("/authenticate").handler(rc -> {
            final String name, pwd;
            try {
                String namePwd = new String(Base64.getDecoder().decode(rc.request().getHeader(HttpHeaders.AUTHORIZATION)), StandardCharsets.UTF_8);
                int sepIdx = namePwd.indexOf(':');
                name = namePwd.substring(0, sepIdx);
                pwd = namePwd.substring(sepIdx + 1);
            }
            catch(Exception e) {
                log.info("Failed to get user/pwd - " + e.getMessage());
                closeWithStatus(rc.response(), HttpResponseStatus.BAD_REQUEST);
                return;
            }

            vertx.fileSystem().readFile("../local/bookings.json", result -> {
                 if (result.succeeded()) {
                     final List<BookingInfo> bookings;
                     try {
                         bookings = Json.mapper.readValue(result.result().getBytes(), new TypeReference<List<BookingInfo>>() {});
                         bookings.sort((a,b) -> {
                             return 0;  // TODO figure out booking priority if same email has booked more than once
                         });
                     }
                     catch(IOException e) {
                         log.error("Failed to read booking info", e);
                         closeWithStatus(rc.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                         return;
                     }
                     for(BookingInfo booking: bookings) {

                         if ((booking.bookingReference.equals(name) || booking.email.equals(name)) &&
                                booking.password.equals(pwd)) {

                             java.util.UUID sessionId = Registry.createSession(booking);
                             rc.response().write(Json.encode(sessionId));
                             closeWithStatus(rc.response(), HttpResponseStatus.OK);
                             return;
                         }
                     }
                     closeWithStatus(rc.response(), HttpResponseStatus.FORBIDDEN);
                 }
                 else {
                     log.warn("Failed to read 'users.txt' file", result.cause());
                     closeWithStatus(rc.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                 }
            });
        });

        return ret;
    }
}
