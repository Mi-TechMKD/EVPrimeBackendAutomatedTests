package data;

import models.request.PostUpdateEventRequest;

public class PostEventDataFactory {

    PostUpdateEventRequest request;

    public PostEventDataFactory(PostUpdateEventRequest requestBody){
        request = requestBody;
    }

    public PostEventDataFactory setTitle(String value){
        request.setTitle(value);
        return this;
    }

    public PostEventDataFactory setImage(String value){
        request.setImage(value);
        return this;
    }

    public PostEventDataFactory setDate(String value){
        request.setDate(value);
        return this;
    }

    public PostEventDataFactory setDescription(String value){
        request.setDescription(value);
        return this;
    }

    public PostEventDataFactory setLocation(String value){
        request.setLocation(value);
        return this;
    }

    public PostUpdateEventRequest createRequest(){
        return request;
    }

}
