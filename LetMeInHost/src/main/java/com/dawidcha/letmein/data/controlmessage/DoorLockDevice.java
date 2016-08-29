package com.dawidcha.letmein.data.controlmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DoorLockDevice extends Device {
    public final boolean isLocked;

    public DoorLockDevice(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("isLocked") boolean isLocked
    ) {
        super(id, name);
        this.isLocked = isLocked;
    }
}
