package com.dawidcha.letmein.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;

public class HubInfo {
    public final String id;

    public HubInfo(
            @JsonProperty("id") String id
    ) {
        this.id = id;
    }
}
