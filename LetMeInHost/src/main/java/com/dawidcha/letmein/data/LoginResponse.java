package com.dawidcha.letmein.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginResponse {
    public final String sessionId;
    public final BookingInfo bookingInfo;

    public LoginResponse(
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("bookingInfo") BookingInfo bookingInfo
    ) {
        this.sessionId = sessionId;
        this.bookingInfo = bookingInfo;
    }
}
