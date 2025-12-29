package objectbuilder;

import models.request.PostUpdateEventRequest;

public class PostEventObjectBuilder {

    public static PostUpdateEventRequest createBodyForPostEvent() {
        return PostUpdateEventRequest.builder()
                .title("default@mail.com")
                .image("default password")
                .date("07.11.2023")
                .location("default location")
                .description("default description")
                .build();
    }

}
