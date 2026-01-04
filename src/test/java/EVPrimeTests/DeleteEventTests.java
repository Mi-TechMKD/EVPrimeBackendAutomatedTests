package EVPrimeTests;

import client.EVPrimeClient;
import data.PostEventDataFactory;
import data.SignUpLoginDataFactory;
import database.DBClient;
import io.restassured.response.Response;
import models.request.PostUpdateEventRequest;
import models.request.SignUpLoginRequest;
import models.response.LoginResponse;
import models.response.PostUpdateDeleteEventResponse;
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
import static org.junit.Assert.*;

public class DeleteEventTests {

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
    public void successfulDeleteEventTest() throws SQLException {
        Response responseDelete = new EVPrimeClient().deleteEvent(loginResponseBody.getToken(), eventId);

        PostUpdateDeleteEventResponse deleteResponseBody = responseDelete.body().as(PostUpdateDeleteEventResponse.class);

        assertEquals(200, responseDelete.statusCode());
        assertEquals("Successfully deleted the event with id: " + eventId, deleteResponseBody.getMessage());
        assertFalse(dbClient.doesEventExist(eventId));
    }
    @Test
    public void failDeleteMissingTokenTest() {

        Response deleteResponse = client.deleteEvent("", "1");

        PostUpdateDeleteEventResponse responseBody = deleteResponse.body().as(PostUpdateDeleteEventResponse.class);

        assertEquals(401, deleteResponse.statusCode());
        assertEquals("Not authenticated.", responseBody.getMessage());
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
