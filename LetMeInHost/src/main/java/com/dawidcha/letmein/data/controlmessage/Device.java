package com.dawidcha.letmein.data.controlmessage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DoorLockDevice.class, name = "doorLock"),
        @JsonSubTypes.Type(value = ResponseMessage.class, name = "response")})
public class Device {
    public final String id;
    public final String name;

    public Device(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name
    ) {
        this.id = id;
        this.name = name;
    }
}
