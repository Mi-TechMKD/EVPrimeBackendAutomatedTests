package EVPrimeTests;

import client.EVPrimeClient;
import io.restassured.response.Response;
import models.request.PostUpdateEventRequest;
import models.request.SignUpLoginRequest;
import models.response.GetEventsResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class GetSingleEventTests {

    private SignUpLoginRequest signUpRequest;
    private PostUpdateEventRequest requestBody;
    private EVPrimeClient client;
    private String token;
    private String eventId;
    private String email;
    private String password;

    @Before
    public void setup() {
        client = new EVPrimeClient();
        email = "testUser@mail.com";
        password = "Password123";

        SignUpLoginRequest signupBody = new SignUpLoginRequest(email, password);
        client.signUp(signupBody);

        token = client.login(signupBody).jsonPath().getString("token");

        requestBody = new PostUpdateEventRequest(
                "Test Event",
                "https://example.com/test.jpg",
                "29.12.2025",
                "Skopje",
                "This is a test event"
        );

        Response postResponse = client.postEvent(requestBody, token);

        String message = postResponse.jsonPath().getString("message");
        eventId = message.substring(message.lastIndexOf(":") + 2).trim();
    }

    @Test
    public void getEventByIdTest() {
        Response getEventResponse = client.getEventById(eventId);
        GetEventsResponse responseBody = getEventResponse.body().as(GetEventsResponse.class);

        assertEquals(200, getEventResponse.statusCode());
        assertEquals(1, responseBody.getEvents().size());

        assertEquals(requestBody.getTitle(), responseBody.getEvents().get(0).getTitle());
        assertEquals(requestBody.getDate(), responseBody.getEvents().get(0).getDate());
        assertEquals(requestBody.getImage(), responseBody.getEvents().get(0).getImage());
        assertEquals(requestBody.getDescription(), responseBody.getEvents().get(0).getDescription());
        assertEquals(requestBody.getLocation(), responseBody.getEvents().get(0).getLocation());
    }

    @Test
    public void getNonExistentEventTest() {
        String invalidEventId = "999";

        Response getEventResponse = client.getEventById(invalidEventId);
        GetEventsResponse responseBody = getEventResponse.body().as(GetEventsResponse.class);

        assertEquals(200, getEventResponse.statusCode());
        assertTrue(responseBody.getEvents().isEmpty());
    }

    @After
    public void cleanup() {
        client.deleteEvent(token, eventId);
        client.deleteUser(email, token);
    }
}