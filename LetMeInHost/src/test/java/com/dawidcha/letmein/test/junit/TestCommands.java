package com.dawidcha.letmein.test.junit;

import com.dawidcha.letmein.data.BookingInfo;
import com.dawidcha.letmein.data.LoginResponse;
import com.dawidcha.letmein.test.fixtures.HttpClient;
import com.dawidcha.letmein.test.fixtures.MockHostServer;
import com.dawidcha.letmein.test.fixtures.MockHubAgent;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.http.CaseInsensitiveHeaders;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Base64;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TestCommands {
    private static final boolean showOutput = true;
    private static final String hubId = "be05cf36-289c-49d5-b05a-b3075f9f93da";
    private static final Base64.Encoder encoder = Base64.getEncoder();

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
    public void testHubAgentActions() throws Exception {
        MockHubAgent hubAgent = new MockHubAgent(showOutput);
        hubAgent.setServerPort(hostServer.getServerPort());
        hubAgent.setHubId(hubId);

        hubAgent.start();
        try {
            assertEquals("connect was successful", hubId, Json.mapper.readValue(hubAgent.popNextMessage(), Object.class));

            HttpClient.ReturnData ret;
            MultiMap headers;

            // Log in with hardcoded username/password, retrieve the session id and set it onto the client
            byte[] userPwd = "AAAAA:password".getBytes();
            headers = new CaseInsensitiveHeaders()
                    .add(HttpHeaders.AUTHORIZATION, "Basic " + encoder.encodeToString(userPwd));
            ret = client.request(HttpMethod.GET, "/login/authenticate", headers);
            assertEquals("Returns OK", ret.statusCode, HttpResponseStatus.OK.code());

            LoginResponse loginResponse = Json.mapper.readValue(ret.data, LoginResponse.class);
            client.setSessionId(loginResponse.sessionId);

            ret = client.request(HttpMethod.PUT, "/control/DoorLock1/LockState", null, "Locked");

            System.out.println(ret);
        }
        finally {
            hubAgent.stop();
        }
    }
}
