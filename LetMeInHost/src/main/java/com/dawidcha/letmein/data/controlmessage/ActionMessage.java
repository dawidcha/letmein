package com.dawidcha.letmein.data.controlmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActionMessage extends BaseMessage {

    public final String deviceId;
    public final String action;

    public ActionMessage(
            @JsonProperty("correlationId") int correlationId,
            @JsonProperty("deviceId") String deviceId,
            @JsonProperty("action") String action
    ) {
        super(correlationId);

        this.deviceId = deviceId;
        this.action = action;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
