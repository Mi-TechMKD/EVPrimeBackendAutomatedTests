package models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateDeleteEventResponse {

    String message;
    PostUpdateDeleteEventResponse errors;
    String title;
    String image;
    String date;
    String description;
    String location;

}
