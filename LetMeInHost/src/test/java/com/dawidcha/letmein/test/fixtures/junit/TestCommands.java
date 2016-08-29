package com.dawidcha.letmein.test.fixtures.junit;

import com.dawidcha.letmein.test.fixtures.HttpClient;
import com.dawidcha.letmein.test.fixtures.MockHostServer;
import com.dawidcha.letmein.test.fixtures.MockHubAgent;
import io.vertx.core.http.HttpMethod;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        client.start();
    }

    @After
    public void tearDown() throws Exception {
        client.stop();
        hostServer.stop();
    }

    @Test
    public void testHubAgentFailConnect() {
        MockHubAgent hubAgent = new MockHubAgent(showOutput);
        hubAgent.setServerPort(hostServer.getServerPort());
        hubAgent.setHubId("no-such-id");

        hubAgent.start();
        try {
            assertEquals("fail to connect with bad hub id", "asdf", new String(hubAgent.popNextMessage()));
        }
        finally {
            hubAgent.stop();
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
