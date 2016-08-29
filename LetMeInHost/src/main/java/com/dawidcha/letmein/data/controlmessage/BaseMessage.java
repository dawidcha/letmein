package com.dawidcha.letmein.data.controlmessage;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "message")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StatusMessage.class, name = "status"),
        @JsonSubTypes.Type(value = ActionMessage.class, name = "action"),
        @JsonSubTypes.Type(value = ResponseMessage.class, name = "response")})
public abstract class BaseMessage {
    public interface Visitor {
        public void visit(ActionMessage action);
        public void visit(ResponseMessage response);
        public void visit(StatusMessage status);
    }

    public final int correlationId;

    public BaseMessage(
            @JsonProperty("correlationId") int correlationId
    ) {
        this.correlationId = correlationId;
    }

    public abstract void accept(Visitor visitor);
}