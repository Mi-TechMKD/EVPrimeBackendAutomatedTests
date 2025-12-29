package EVPrimeTests;

import client.EVPrimeClient;
import data.PostEventDataFactory;
import data.SignUpLoginDataFactory;
import database.DBClient;
import io.restassured.response.Response;
import jdk.jfr.Description;
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

public class PostEventTests {

    DBClient dbClient = new DBClient();
    private static String id;
    private static SignUpLoginRequest signUpRequest;
    private static LoginResponse loginResponseBody;
    private static PostUpdateEventRequest requestBody;
    static DateBuilder dateBuilder = new DateBuilder();
    private EVPrimeClient client;

    @Before
    public void setUp() {
        client = new EVPrimeClient();

        signUpRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail(RandomStringUtils.randomAlphanumeric(10) + dateBuilder.currentTimeMinusOneHour() + "@mail.com")
                .setPassword(RandomStringUtils.randomAlphanumeric(10))
                .createRequest();

        new EVPrimeClient()
                .signUp(signUpRequest);

        Response loginResponse = new EVPrimeClient()
                .login(signUpRequest);

        loginResponseBody = loginResponse.body().as(LoginResponse.class);

        requestBody = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.goal.com%2Fen-sg%2Fnews%2Fliverpool-vs-manchester-united-lineups-live-updates%2Fbltf4a9e3c54804c6b8&psig=AOvVaw11pYwQiECKpPWu17jL6s6X&ust=1712771074871000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCOiy883XtYUDFQAAAAAdAAAAABAE")
                .setDate(dateBuilder.currentTime())
                .setLocation(RandomStringUtils.randomAlphanumeric(15))
                .setDescription(RandomStringUtils.randomAlphanumeric(20))
                .createRequest();
    }

    @Test
    public void successfulPostEventTest() throws SQLException {

        Response response = new EVPrimeClient()
                .postEvent(requestBody, loginResponseBody.getToken());

        PostUpdateDeleteEventResponse postResponse = response.body().as(PostUpdateDeleteEventResponse.class);
        id = postResponse.getMessage().substring(39);

        assertEquals(201, response.statusCode());
        assertTrue(postResponse.getMessage().contains("Successfully created an event with id: "));
        assertEquals(requestBody.getTitle(), dbClient.getEventFromDB(postResponse.getMessage().substring(39)).getTitle());
        assertEquals(requestBody.getImage(), dbClient.getEventFromDB(postResponse.getMessage().substring(39)).getImage());
        assertEquals(requestBody.getDate(), dbClient.getEventFromDB(postResponse.getMessage().substring(39)).getDate());
        assertEquals(requestBody.getLocation(), dbClient.getEventFromDB(postResponse.getMessage().substring(39)).getLocation());
        assertEquals(requestBody.getDescription(), dbClient.getEventFromDB(postResponse.getMessage().substring(39)).getDescription());
    }

    @Test
    public void unsuccessfulRequestAuthorizationTokenTest() {

        new EVPrimeClient().signUp(signUpRequest);

        Response response = new EVPrimeClient().postEvent(requestBody, "invalid_or_no_token");

        assertEquals(401, response.statusCode());
        assertEquals("Not authenticated.", response.as(PostUpdateDeleteEventResponse.class).getMessage());

    }

    @Test
    public void invalidTitleTest() {
        PostUpdateEventRequest requestBodyWithInvalidTitle = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle("")
                .setImage("https://example.com/image.jpg")
                .setDate(dateBuilder.currentTime())
                .setLocation(RandomStringUtils.randomAlphanumeric(15))
                .setDescription(RandomStringUtils.randomAlphanumeric(20))
                .createRequest();

        Response response = new EVPrimeClient().postEvent(requestBodyWithInvalidTitle, loginResponseBody.getToken());

        assertEquals(422, response.statusCode());
        assertEquals("Adding the event failed due to validation errors.", response.as(PostUpdateDeleteEventResponse.class).getMessage());
        assertEquals("Invalid title.", response.as(PostUpdateDeleteEventResponse.class).getErrors().getTitle());
    }

    @Test
    public void invalidImageEmptyTest() {
        PostUpdateEventRequest invalid = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(requestBody.getTitle())
                .setImage("")
                .setDate(requestBody.getDate())
                .setLocation(requestBody.getLocation())
                .setDescription(requestBody.getDescription())
                .createRequest();

        Response response = client.postEvent(invalid, loginResponseBody.getToken());

        assertEquals(422, response.statusCode());
        PostUpdateDeleteEventResponse errors = response.as(PostUpdateDeleteEventResponse.class);
        assertEquals("Invalid image.", errors.getErrors().getImage());
    }

    @Test
    public void invalidImageFormatTest() {
        PostUpdateEventRequest invalid = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(requestBody.getTitle())
                .setImage("evsummit.jpg")
                .setDate(requestBody.getDate())
                .setLocation(requestBody.getLocation())
                .setDescription(requestBody.getDescription())
                .createRequest();

        Response response = client.postEvent(invalid, loginResponseBody.getToken());

        assertEquals(422, response.statusCode());
        PostUpdateDeleteEventResponse errors = response.as(PostUpdateDeleteEventResponse.class);
        assertEquals("Invalid image.", errors.getErrors().getImage());
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

        Response response = new EVPrimeClient().postEvent(requestBodyWithInvalidDate, loginResponseBody.getToken());

        assertEquals(422, response.statusCode());
        assertEquals("Adding the event failed due to validation errors.", response.as(PostUpdateDeleteEventResponse.class).getMessage());
        assertEquals("Invalid date.", response.as(PostUpdateDeleteEventResponse.class).getErrors().getDate());
    }

    @Test
    @Ignore
    @Description("Possible bug in backend: empty location error is returned in description field")
    public void invalidLocationTest() {
        PostUpdateEventRequest requestBodyWithInvalidLocation = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle("Some title")
                .setImage("https://picture.jpg")
                .setDate("12.10.2025")
                .setLocation("")
                .setDescription("Some Description")
                .createRequest();

        Response response = new EVPrimeClient().postEvent(requestBodyWithInvalidLocation, loginResponseBody.getToken());

        assertEquals(422, response.statusCode());
        assertEquals("Adding the event failed due to validation errors.", response.as(PostUpdateDeleteEventResponse.class).getMessage());
        assertEquals("Invalid location.", response.as(PostUpdateDeleteEventResponse.class).getErrors().getDescription());
    }

    @Test
    public void invalidDescriptionTest() {
        PostUpdateEventRequest requestBodyWithInvalidDescription = new PostEventDataFactory(createBodyForPostEvent())
                .setTitle(RandomStringUtils.randomAlphanumeric(10))
                .setImage("https://picture.jpg")
                .setDate(dateBuilder.currentTime())
                .setLocation(RandomStringUtils.randomAlphanumeric(15))
                .setDescription("")
                .createRequest();

        Response response = new EVPrimeClient().postEvent(requestBodyWithInvalidDescription, loginResponseBody.getToken());

        assertEquals(422, response.statusCode());
        assertEquals("Adding the event failed due to validation errors.", response.as(PostUpdateDeleteEventResponse.class).getMessage());
        assertEquals("Invalid description.", response.as(PostUpdateDeleteEventResponse.class).getErrors().getDescription());
    }

    @Test
    public void missingTokenTest() {
        Response response = client.postEvent(requestBody, "");

        assertEquals(401, response.statusCode());
        PostUpdateDeleteEventResponse resp = response.as(PostUpdateDeleteEventResponse.class);
        assertEquals("Not authenticated.", resp.getMessage());
    }

    @After
    public void deleteEvent() throws SQLException {
        if (id != null) {
            new DBClient().isEventDeletedFromDb(id);
        }
    }
}