package util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateBuilder {

    public String currentTimeMinusOneHour() {
        ZonedDateTime date = ZonedDateTime.now().minusHours(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return date.format(formatter);
    }

    public String currentTime() {
        ZonedDateTime date = ZonedDateTime.now().minusHours(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

}
