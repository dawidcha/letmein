package com.dawidcha.letmein.data.controlmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PulseDevice extends Device {

    public PulseDevice(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name
    ) {
        super(id, name);
    }
}
