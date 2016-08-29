package com.dawidcha.letmein.data.controlmessage;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

public class ResponseMessage extends BaseMessage {

    public final Object response;

    public ResponseMessage(
            @JsonProperty("correlationId") int correlationId,
            @JsonProperty("response") Object response
    ) {
        super(correlationId);
        this.response = response;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
