package com.dawidcha.letmein;

import com.dawidcha.letmein.data.BookingInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Registry {
    private static Map<UUID, BookingInfo> sessions = new HashMap<>();
    private static Map<BookingInfo, UUID> reverseSessions = new HashMap<>();

    public static UUID createSession(BookingInfo forBooking) {
        UUID sessionId = UUID.randomUUID();
        sessions.put(sessionId, forBooking);
        reverseSessions.put(forBooking, sessionId);

        return sessionId;
    }
}
