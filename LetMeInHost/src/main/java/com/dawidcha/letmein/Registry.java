package com.dawidcha.letmein;

import com.dawidcha.letmein.data.BookingInfo;
import com.dawidcha.letmein.data.HubInfo;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Registry {
    private static Logger log = LoggerFactory.getLogger(Registry.class);

    private static Map<UUID, BookingInfo> sessions = new HashMap<>();
    private static Map<BookingInfo, UUID> reverseSessions = new HashMap<>();

    private static Map<String, ServerWebSocket> hubConnections = new HashMap<>();
    private static Map<ServerWebSocket, HubInfo> reverseHubConnections = new HashMap<>();

    public synchronized static UUID createSession(BookingInfo forBooking) {
        UUID sessionId = UUID.randomUUID();
        sessions.put(sessionId, forBooking);
        reverseSessions.put(forBooking, sessionId);

        return sessionId;
    }

    public synchronized static BookingInfo bookingInfoForSession(UUID sessionId) {
        return sessions.get(sessionId);
    }

    public synchronized static void registerHubConnection(HubInfo hub, ServerWebSocket webSocket) {
        log.info("Made connection to hub '" + hub.id + "' (" + webSocket.remoteAddress().toString() + ")");

        hubConnections.put(hub.id, webSocket);
        reverseHubConnections.put(webSocket, hub);
    }

    public synchronized static void closeHubConnection(ServerWebSocket webSocket) {
        HubInfo hub = reverseHubConnections.get(webSocket);
        if(hub != null) {
            log.info("Dropping connection from hub '" + hub.id + "' (" + webSocket.remoteAddress().toString() + ")");

            reverseHubConnections.remove(webSocket);
            hubConnections.remove(hub.id);
        }
    }
}
