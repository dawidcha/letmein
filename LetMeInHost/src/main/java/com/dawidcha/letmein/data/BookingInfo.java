package com.dawidcha.letmein.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;

public class BookingInfo {
    public final String bookingReference;
    public final String hubId;
    public final String email;
    public final String name;
    public final String password;
    @JsonSerialize(using = JsonSerialization.FromLocalDateTime.class) public final LocalDateTime from;
    @JsonSerialize(using = JsonSerialization.FromLocalDateTime.class) public final LocalDateTime to;

    public BookingInfo(
            @JsonProperty("bookingReference") String bookingReference,
            @JsonProperty("hubId") String hubId,
            @JsonProperty("email") String email,
            @JsonProperty("name") String name,
            @JsonProperty("password") String password,
            @JsonProperty("from") @JsonDeserialize(using = JsonSerialization.ToLocalDateTime.class) LocalDateTime from,
            @JsonProperty("to") @JsonDeserialize(using = JsonSerialization.ToLocalDateTime.class) LocalDateTime to
    ) {
        this.bookingReference = bookingReference;
        this.hubId = hubId;
        this.email = email;
        this.name = name;
        this.password = password;
        this.from = from;
        this.to = to;
    }

    public boolean equals(Object o) {
        if (o.getClass() != getClass()) {
            return false;
        }
        BookingInfo other = (BookingInfo)o;

        return
            bookingReference.equals(other.bookingReference) &&
            email.equals(other.email) &&
            from.equals(other.from) &&
            to.equals(other.to);
    }

    public int hashCode() {
        return bookingReference.hashCode() + email.hashCode() + from.hashCode() + to.hashCode();
    }
}
