package com.dawidcha.letmein.handlers;

import com.dawidcha.letmein.Registry;
import com.dawidcha.letmein.data.BookingInfo;
import com.dawidcha.letmein.data.LoginResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login {
    private static final Logger log = LoggerFactory.getLogger(Login.class);
    private static final Pattern basicAuth = Pattern.compile("^Basic (.+)$");
    private static final File local = new File("local").isDirectory()? new File("local"): new File("../local");

    public static Router makeRouter(Vertx vertx) {

        Router ret = Router.router(vertx);

        ret.get("/authenticate").handler(rc -> {
            final String name, pwd;
            try {
                Matcher matcher = basicAuth.matcher(rc.request().getHeader(HttpHeaders.AUTHORIZATION));
                if (!matcher.find()) {
                    throw new IllegalArgumentException("Use 'Basic' authentication");
                }
                String namePwd = new String(Base64.getDecoder().decode(matcher.group(1)), StandardCharsets.UTF_8);
                int sepIdx = namePwd.indexOf(':');
                name = namePwd.substring(0, sepIdx);
                pwd = namePwd.substring(sepIdx + 1);
            }
            catch(Exception e) {
                log.info("Failed to get user/pwd - " + e.getMessage());
                ApiRouters.closeWithStatus(rc.response(), HttpResponseStatus.BAD_REQUEST);
                return;
            }

            vertx.fileSystem().readFile(local.getAbsolutePath() + "/bookings.json", result -> {
                 if (result.succeeded()) {
                     try {
                         final List<BookingInfo> bookings;
                         bookings = Json.mapper.readValue(result.result().getBytes(), new TypeReference<List<BookingInfo>>() {});
                         bookings.sort((a,b) -> {
                             return 0;  // TODO figure out booking priority if same email has booked more than once
                         });
                         for(BookingInfo booking: bookings) {

                             if ((booking.bookingReference.equals(name) || booking.email.equals(name)) &&
                                    booking.password.equals(pwd)) {

                                 UUID sessionId = Registry.createSession(booking);

                                 LoginResponse response = new LoginResponse(sessionId.toString(), booking);
                                 byte[] responseBytes = Json.mapper.writeValueAsBytes(response);
                                 rc.response().headers().add(HttpHeaders.CONTENT_LENGTH, Integer.toString(responseBytes.length));
                                 rc.response().write(Buffer.buffer(responseBytes));
                                 ApiRouters.closeWithStatus(rc.response(), HttpResponseStatus.OK);
                                 return;
                             }
                         }
                         ApiRouters.closeWithStatus(rc.response(), HttpResponseStatus.FORBIDDEN);
                     }
                     catch(Exception e) {
                         log.error("Failed to read booking info", e);
                         ApiRouters.closeWithStatus(rc.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                     }
                 }
                 else {
                     log.warn("Failed to read 'users.txt' file", result.cause());
                     ApiRouters.closeWithStatus(rc.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR);
                 }
            });
        });

        return ret;
    }
}
