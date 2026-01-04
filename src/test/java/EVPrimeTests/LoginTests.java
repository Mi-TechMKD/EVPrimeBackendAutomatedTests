package EVPrimeTests;

import client.EVPrimeClient;
import data.SignUpLoginDataFactory;
import database.DBClient;
import io.restassured.response.Response;
import models.request.SignUpLoginRequest;
import models.response.LoginResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.DateBuilder;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static objectbuilder.SignUpObjectBuilder.createBodyForSignUp;
import static org.junit.Assert.*;

public class LoginTests {

    private EVPrimeClient client;
    private SignUpLoginRequest signUpRequest;
    private String existingEmail;
    private String existingPassword;
    private String authToken;
    private DateBuilder dateBuilder;
    private String createdEmail;

    @Before
    public void setUp() {
        client = new EVPrimeClient();
        dateBuilder = new DateBuilder();


        existingEmail = RandomStringUtils.randomAlphanumeric(10) + "@mail.com";
        existingPassword = RandomStringUtils.randomAlphanumeric(10);
        createdEmail = existingEmail;

        signUpRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail(existingEmail)
                .setPassword(existingPassword)
                .createRequest();

        Response signUpResponse = client.signUp(signUpRequest);
        assertEquals(201, signUpResponse.statusCode());

        Response loginResponse = client.login(signUpRequest);
        LoginResponse loginRespBody = loginResponse.body().as(LoginResponse.class);
        authToken = loginRespBody.getToken();
    }

    @Test
    public void successfulLoginTest() {
        Response response = new EVPrimeClient().login(signUpRequest);
        LoginResponse loginResponse = response.body().as(LoginResponse.class);

        assertEquals(200, response.statusCode());
        assertNotNull(loginResponse.getToken());

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime expiration = LocalDateTime.parse(loginResponse.getExpirationTime(), formatter);
            LocalDateTime nowMinusOneHour = LocalDateTime.now().minusHours(1);

            assertTrue("Expiration time should be after current time minus one hour",
                    expiration.isAfter(nowMinusOneHour));
        } catch (Exception e) {
            fail("Failed to parse expiration time: " + loginResponse.getExpirationTime());
        }
    }

    @Test
    public void loginFailsWithNonExistentEmailTest() {
        SignUpLoginRequest loginRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail("nonexistingemail@mail.com")
                .setPassword(existingPassword)
                .createRequest();

        Response response = new EVPrimeClient().login(loginRequest);

        assertEquals(401, response.statusCode());
        assertEquals("Authentication failed.",
                response.jsonPath().getString("message"));
    }

    @Test
    public void loginFailsWithEmptyEmailTest() {
        SignUpLoginRequest loginRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail("")
                .setPassword(existingPassword)
                .createRequest();

        Response response = new EVPrimeClient().login(loginRequest);

        assertEquals(401, response.statusCode());
        assertEquals("Authentication failed.",
                response.jsonPath().getString("message"));
    }

    @Test
    public void loginFailsWithWrongPasswordTest() {
        SignUpLoginRequest loginRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail(existingEmail)
                .setPassword("WrongPassword123")
                .createRequest();

        Response response = new EVPrimeClient().login(loginRequest);

        assertEquals(422, response.statusCode());
        assertEquals("Invalid credentials.",
                response.jsonPath().getString("message"));
        assertEquals("Invalid email or password entered.",
                response.jsonPath().getString("errors.credentials"));
    }

    @Test
    public void loginFailsWithEmptyPasswordTest() {
        SignUpLoginRequest loginRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail(existingEmail)
                .setPassword("")
                .createRequest();

        Response response = new EVPrimeClient().login(loginRequest);

        assertEquals(422, response.statusCode());
        assertEquals("Invalid credentials.",
                response.jsonPath().getString("message"));
        assertEquals("Invalid email or password entered.",
                response.jsonPath().getString("errors.credentials"));
    }

    @After
    public void tearDown() throws SQLException {
        DBClient dbClient = new DBClient();

        if (createdEmail != null) {
            dbClient.deleteUserByEmail(createdEmail);
            createdEmail = null;
        }
    }
}