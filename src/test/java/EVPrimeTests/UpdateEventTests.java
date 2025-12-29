package evprimeTests;

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
import org.junit.*;
import util.DateBuilder;

import java.sql.SQLException;

import static objectbuilder.PostEventObjectBuilder.createBodyForPostEvent;
import static objectbuilder.SignUpObjectBuilder.createBodyForSignUp;
import static org.junit.Assert.*;

public class UpdateEventTests {

    DBClient dbClient = new DBClient();
    private static String eventId;
    private static SignUpLoginRequest signUpRequest;
    private static LoginResponse loginResponseBody;
    private static PostUpdateEventRequest requestBody;
    static DateBuilder dateBuilder = new DateBuilder();
    private EVPrimeClient client;
    private static PostUpdateDeleteEventResponse postResponse;

    @Before
    public void setUp() throws SQLException {
        client = new EVPrimeClient();

        signUpRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail(RandomStringUtils.randomAlphanumeric(10) + dateBuilder.currentTimeMinusOneHour() + "@mail.com")
                .setPassword(RandomStringUtils.randomAlphanumeric(10))
                .createRequest();

        new EVPrimeClient().signUp(signUpRequest);
        Response loginResponse = new EVPrimeClient().login(signUpRequest);
        loginResponseBody = loginResponse.body().as(LoginResponse.class);

        requestBody = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("https://example.com/evsummit-updated.jpg")
                .setDate(dateBuilder.currentTime())
                .setLocation(RandomStringUtils.randomAlphanumeric(15))
                .setDescription(RandomStringUtils.randomAlphanumeric(20))
                .createRequest();

        Response postResponseRaw = client.postEvent(requestBody, loginResponseBody.getToken());
        assertEquals("Event creation FAILED!", 201, postResponseRaw.statusCode());

        postResponse = postResponseRaw.body().as(PostUpdateDeleteEventResponse.class);
        eventId = postResponse.getMessage().substring(39);
    }

    @Test
    public void SuccessfulUpdateEventTest() throws SQLException {
        requestBody.setDate("2025-12-08");

        Response updateResponse = new EVPrimeClient()
                .updateEvent(requestBody, loginResponseBody.getToken(), postResponse.getMessage().substring(39));

        PostUpdateDeleteEventResponse updateResponseBody = updateResponse.body().as(PostUpdateDeleteEventResponse.class);

        assertEquals(201, updateResponse.statusCode());
        assertTrue(updateResponseBody.getMessage().contains("Successfully updated the event with id: "));
        assertEquals(requestBody.getDate(), dbClient.getEventFromDB(postResponse.getMessage().substring(39)).getDate());

    }

    @Test
    public void failUpdateEmptyFieldsTest() {
        PostUpdateEventRequest updateRequest = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle("")
                .setImage("https://example.com/evsummit-updated.jpg")
                .setDate("2026-01-20")
                .setLocation("Ohrid")
                .setDescription("")
                .createRequest();

        Response response = client.updateEvent(updateRequest, loginResponseBody.getToken(), eventId);
        PostUpdateDeleteEventResponse body = response.body().as(PostUpdateDeleteEventResponse.class);

        assertEquals(422, response.statusCode());
        assertEquals("Updating the event failed due to validation errors.", body.getMessage());
        assertEquals("Invalid title.", body.getErrors().getTitle());
        assertEquals("Invalid description.", body.getErrors().getDescription());
    }

    @Test
    public void invalidDateTest() {
        PostUpdateEventRequest requestBodyWithInvalidDate = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("https://picture.jpg")
                .setDate("")
                .setLocation(RandomStringUtils.randomAlphanumeric(15))
                .setDescription(RandomStringUtils.randomAlphanumeric(20))
                .createRequest();

        Response updateResponse = new EVPrimeClient()
                .updateEvent(requestBodyWithInvalidDate, loginResponseBody.getToken(), eventId);

        assertEquals(422, updateResponse.statusCode());
        assertEquals("Updating the event failed due to validation errors.", updateResponse.as(PostUpdateDeleteEventResponse.class).getMessage());
        assertEquals("Invalid date.", updateResponse.as(PostUpdateDeleteEventResponse.class).getErrors().getDate());
    }

    @Test
    public void invalidLocationTest() {
        PostUpdateEventRequest requestBodyWithInvalidLocation = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("https://picture.jpg")
                .setDate("2025-12-29")
                .setLocation("") // празна локација
                .setDescription(RandomStringUtils.randomAlphanumeric(20))
                .createRequest();

        Response updateResponse = client.updateEvent(requestBodyWithInvalidLocation, loginResponseBody.getToken(), eventId);

        assertEquals(422, updateResponse.statusCode());
        PostUpdateDeleteEventResponse responseBody = updateResponse.as(PostUpdateDeleteEventResponse.class);
        assertEquals("Updating the event failed due to validation errors.", responseBody.getMessage());

        //BUG temporary solution
        String locationError = responseBody.getErrors().getLocation();
        if(locationError == null) {
            locationError = responseBody.getErrors().getDescription();
        }
        assertEquals("Invalid location.", locationError);
    }

    @Test
    public void invalidDescriptionTest() {
        PostUpdateEventRequest requestBodyWithInvalidDescription = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("https://picture.jpg")
                .setDate(dateBuilder.currentTime())
                .setLocation(RandomStringUtils.randomAlphanumeric(20))
                .setDescription("")
                .createRequest();

        Response updateResponse = new EVPrimeClient()
                .updateEvent(requestBodyWithInvalidDescription, loginResponseBody.getToken(), eventId);

        assertEquals(422, updateResponse.statusCode());
        assertEquals("Updating the event failed due to validation errors.", updateResponse.as(PostUpdateDeleteEventResponse.class).getMessage());
        assertEquals("Invalid description.", updateResponse.as(PostUpdateDeleteEventResponse.class).getErrors().getDescription());
    }

    @Test
    public void invalidImageTest() {
        PostUpdateEventRequest requestBodyWithInvalidImage = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("example.com/invalid-image.jpg")
                .setDate(dateBuilder.currentTime())
                .setLocation(RandomStringUtils.randomAlphanumeric(15))
                .setDescription(RandomStringUtils.randomAlphanumeric(20))
                .createRequest();

        Response updateResponse = new EVPrimeClient()
                .updateEvent(requestBodyWithInvalidImage, loginResponseBody.getToken(), eventId);

        assertEquals(422, updateResponse.statusCode());
        assertEquals("Updating the event failed due to validation errors.", updateResponse.as(PostUpdateDeleteEventResponse.class).getMessage());
        assertEquals("Invalid image.", updateResponse.as(PostUpdateDeleteEventResponse.class).getErrors().getImage());
    }

    @Test
    public void unsuccessfulRequestAuthorizationTokenTest() {
        Response response = new EVPrimeClient()
                .updateEvent(requestBody, "invalid_or_no_token", eventId);

        assertEquals(401, response.statusCode());
        assertEquals("Not authenticated.", response.as(PostUpdateDeleteEventResponse.class).getMessage());
    }

    @After
    public void deleteEvent() throws SQLException {
        if (eventId != null) {
            new DBClient().isEventDeletedFromDb(eventId);
        }
    }
}
