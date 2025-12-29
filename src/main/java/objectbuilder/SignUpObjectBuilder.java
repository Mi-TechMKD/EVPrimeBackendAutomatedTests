package objectbuilder;

import models.request.SignUpLoginRequest;

public class SignUpObjectBuilder {

    public static SignUpLoginRequest createBodyForSignUp() {
        return SignUpLoginRequest.builder()
                .email("default@mail.com")
                .password("default password")
                .build();
    }

}
