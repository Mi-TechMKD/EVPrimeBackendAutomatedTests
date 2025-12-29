package EVPrimeTests;

import client.EVPrimeClient;
import data.SignUpLoginDataFactory;
import io.restassured.response.Response;
import models.request.SignUpLoginRequest;
import models.response.SignUpErrorResponse;
import models.response.SignUpResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import static objectbuilder.SignUpObjectBuilder.createBodyForSignUp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SignUpTests {


    @Test
    public void successfulSignUpTest() {
        SignUpLoginRequest signUpRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail(RandomStringUtils.randomAlphanumeric(10) + "@mail.com")
                .setPassword(RandomStringUtils.randomAlphanumeric(10))
                .createRequest();

        Response response = new EVPrimeClient().signUp(signUpRequest);

        SignUpResponse signUpResponse = response.body().as(SignUpResponse.class);

        assertEquals(201, response.statusCode());
        assertEquals("User created.", signUpResponse.getMessage());
        assertNotNull(signUpResponse.getUser().getId());
        assertEquals(signUpRequest.getEmail(), signUpResponse.getUser().getEmail());
        assertNotNull(signUpResponse.getToken());
    }

    @Test
    public void signUpFailsWithInvalidEmailTest() {
        SignUpLoginRequest signUpRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail("invalidEmail.")
                .setPassword("Password123")
                .createRequest();

        Response response = new EVPrimeClient().signUp(signUpRequest);

        assertEquals(422, response.statusCode());
        assertEquals("User signup failed due to validation errors.",
                response.jsonPath().getString("message"));
        assertEquals("Invalid email.",
                response.jsonPath().getString("errors.email"));
    }

    @Test
    public void signUpFailsWithShortPasswordTest() {
        SignUpLoginRequest signUpRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail(RandomStringUtils.randomAlphanumeric(10) + "@mail.com")
                .setPassword("123")
                .createRequest();

        Response response = new EVPrimeClient().signUp(signUpRequest);

        assertEquals(422, response.statusCode());
        assertEquals("User signup failed due to validation errors.",
                response.jsonPath().getString("message"));
        assertEquals("Invalid password. Must be at least 6 characters long.",
                response.jsonPath().getString("errors.password"));
    }

    @Test
    public void signUpFailsWithEmptyEmailTest() {
        SignUpLoginRequest signUpRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail("")
                .setPassword("Password123")
                .createRequest();

        Response response = new EVPrimeClient().signUp(signUpRequest);

        assertEquals(422, response.statusCode());
        assertEquals("User signup failed due to validation errors.",
                response.jsonPath().getString("message"));
        assertEquals("Invalid email.",
                response.jsonPath().getString("errors.email"));
    }

    @Test
    public void signUpFailEmailAlreadyExistTest() {
        SignUpLoginRequest signUpRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail(RandomStringUtils.randomAlphanumeric(10) + "@mail.com")
                .setPassword(RandomStringUtils.randomAlphanumeric(10))
                .createRequest();

        Response firstResponse = new EVPrimeClient().signUp(signUpRequest);
        assertEquals(201, firstResponse.statusCode());

        Response secondResponse = new EVPrimeClient().signUp(signUpRequest);

        SignUpErrorResponse signUpErrorResponse =
                secondResponse.body().as(SignUpErrorResponse.class);

        assertEquals(422, secondResponse.statusCode());
        assertEquals("User signup failed due to validation errors.",
                signUpErrorResponse.getMessage());
        assertEquals("Email exists already.",
                signUpErrorResponse.getErrors().getEmail());
    }

    @Test
    public void signUpFailsWithEmptyPasswordTest() {
        SignUpLoginRequest signUpRequest = new SignUpLoginDataFactory(createBodyForSignUp())
                .setEmail(RandomStringUtils.randomAlphanumeric(10) + "@mail.com")
                .setPassword("")
                .createRequest();

        Response response = new EVPrimeClient().signUp(signUpRequest);

        assertEquals(422, response.statusCode());
        assertEquals("User signup failed due to validation errors.",
                response.jsonPath().getString("message"));
        assertEquals("Invalid password. Must be at least 6 characters long.",
                response.jsonPath().getString("errors.password"));
    }
}
