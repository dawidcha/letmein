package com.dawidcha.letmein.test.junit;

import com.dawidcha.letmein.test.fixtures.HttpClient;
import com.dawidcha.letmein.test.fixtures.MockHostServer;
import com.dawidcha.letmein.test.fixtures.MockHubAgent;
import io.vertx.core.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TestCommands {
    private static boolean showOutput = true;

    private MockHostServer hostServer;
    private HttpClient client;

    @Before
    public void setUp() throws Exception {
        hostServer = new MockHostServer(showOutput);
        hostServer.start();

        client = new HttpClient(showOutput);
        client.setServerPort(hostServer.getServerPort());
        client.setAccept("application/json");
        client.start();
    }

    @After
    public void tearDown() throws Exception {
        client.stop();
        hostServer.stop();
    }

    @Test
    public void testHubAgentFailCantConnect () throws Exception {
        try(MockHubAgent hubAgent = new MockHubAgent(showOutput)
                .setServerPort(hostServer.getServerPort())
                .setHubUri("/no-such-uri")
            ) {
            hubAgent.start();

            hostServer.getProcessDef().waitForMessage(1000, Pattern.compile("Websocket connection to '/no-such-uri' rejected"), false);
            assertNull("fail to connect with invalid connect uri", hubAgent.popNextMessage());
        }

        try(MockHubAgent hubAgent = new MockHubAgent(showOutput)
                .setServerPort(hostServer.getServerPort())
                .setHubId("no-such-id")
        ) {
            hubAgent.start();

            hostServer.getProcessDef().waitForMessage(1000, Pattern.compile("No such hub id 'no-such-id'"), false);
            assertEquals("fail to connect with bad hub id", "Websocket closed by server", new String(hubAgent.popNextMessage()));
        }
    }

    @Test
    public void testHubAgentActions() {
        MockHubAgent hubAgent = new MockHubAgent(showOutput);
        hubAgent.setServerPort(hostServer.getServerPort());
        hubAgent.setHubId("be05cf36-289c-49d5-b05a-b3075f9f93da");

        hubAgent.start();
        try {
            assertNull("connect was successful", hubAgent.popNextMessage());

            HttpClient.ReturnData ret = client.request(HttpMethod.PUT, "/control/DoorLock1/LockState", "Locked");
        }
        finally {
            hubAgent.stop();
        }
    }
}
