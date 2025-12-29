package evprimeTests;

import client.EVPrimeClient;
import io.restassured.response.Response;
import models.request.PostUpdateEventRequest;
import models.request.SignUpLoginRequest;
import models.response.GetEventsResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetAllEventsTests {

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

        PostUpdateEventRequest eventBody = new PostUpdateEventRequest(
                "Test Event for GET All",
                "https://example.com/test.jpg",
                "29.12.2025",
                "Skopje",
                "This is a test event"
        );

        Response postResponse = client.postEvent(eventBody, token);
        eventId = postResponse.jsonPath().getString("id");
    }

    @Test
    public void getAllEventsTest() {
        Response response = client.getAllEvents();
        GetEventsResponse responseBody = response.body().as(GetEventsResponse.class);

        assertEquals(200, response.statusCode());
        assertFalse(responseBody.getEvents().isEmpty());
    }

    @After
    public void cleanup() {
        client.deleteEvent(token, eventId);

        client.deleteUser(email, token);
    }
}