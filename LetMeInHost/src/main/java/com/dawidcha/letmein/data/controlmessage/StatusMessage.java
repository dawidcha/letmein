package com.dawidcha.letmein.data.controlmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

public class StatusMessage extends BaseMessage {

    public final boolean connectedOk;
    public final List<Device> devices;

    public StatusMessage(
            @JsonProperty("correlationId") int correlationId,
            @JsonProperty("connectedOk") boolean connectedOk,
            @JsonProperty("devices") List<Device> devices
    ) {
        super(correlationId);
        this.connectedOk = connectedOk;
        this.devices = Arrays.asList(devices.toArray(new Device[devices.size()]));
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
