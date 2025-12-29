package data;

import models.request.SignUpLoginRequest;

public class SignUpLoginDataFactory {

    SignUpLoginRequest request;

    public SignUpLoginDataFactory() {
        this.request = new SignUpLoginRequest();
    }

    public SignUpLoginDataFactory(SignUpLoginRequest requestBody){
        request = requestBody;
    }

    public SignUpLoginDataFactory setEmail(String value){
        request.setEmail(value);
        return this;
    }

    public SignUpLoginDataFactory setPassword(String value){
        request.setPassword(value);
        return this;
    }

    public SignUpLoginRequest createRequest(){
        return request;
    }

}
