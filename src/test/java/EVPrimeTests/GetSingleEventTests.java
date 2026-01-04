package EVPrimeTests;

import client.EVPrimeClient;
import data.PostEventDataFactory;
import data.SignUpLoginDataFactory;
import database.DBClient;
import io.restassured.response.Response;
import models.request.PostUpdateEventRequest;
import models.request.SignUpLoginRequest;
import models.response.GetEventsResponse;
import models.response.LoginResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.DateBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static objectbuilder.PostEventObjectBuilder.createBodyForPostEvent;
import static objectbuilder.SignUpObjectBuilder.createBodyForSignUp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class GetSingleEventTests {

    private static List<String> createdEmails = new ArrayList<>();
    private static String eventId;
    private static SignUpLoginRequest signUpRequest;
    private static LoginResponse loginResponseBody;
    private static PostUpdateEventRequest requestBody;
    private static DateBuilder dateBuilder = new DateBuilder();

    private EVPrimeClient client;
    private DBClient dbClient;

    @Before
    public void setUp() {
        client = new EVPrimeClient();
        dbClient = new DBClient();

        signUpRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail(RandomStringUtils.randomAlphanumeric(10) + dateBuilder.currentTimeMinusOneHour() + "@mail.com")
                .setPassword(RandomStringUtils.randomAlphanumeric(10))
                .createRequest();

        createdEmails.add(signUpRequest.getEmail());

        client.signUp(signUpRequest);

        Response loginResponse = client.login(signUpRequest);
        loginResponseBody = loginResponse.body().as(LoginResponse.class);

        requestBody = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.goal.com%2Fen-sg%2Fnews%2Fliverpool-vs-manchester-united-lineups-live-updates%2Fbltf4a9e3c54804c6b8")
                .setDate(dateBuilder.currentTime())
                .setLocation(RandomStringUtils.randomAlphanumeric(15))
                .setDescription(RandomStringUtils.randomAlphanumeric(20))
                .createRequest();

        Response postResponse = client.postEvent(requestBody, loginResponseBody.getToken());
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
    public void tearDown() throws SQLException {
        if (eventId != null) {
            dbClient.deleteEventById(eventId);
            eventId = null;
        }

        for (String email : createdEmails) {
            dbClient.deleteUserByEmail(email);
        }
        createdEmails.clear();
    }

}