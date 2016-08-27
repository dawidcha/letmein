package com.dawidcha.letmein;

import io.vertx.core.http.HttpClientRequest;

public class ActionCommands {
    private final HttpClientRequest forRequest;

    public ActionCommands(HttpClientRequest forRequest) {
        this.forRequest = forRequest;
    }

    public void openDoor() {
        forRequest.end("openDoor");
    }

    public void closeDoor() {
        forRequest.end("closeDoor");
    }

    public void triggerLatch() {
        forRequest.end("triggerLatch");
    }
}
