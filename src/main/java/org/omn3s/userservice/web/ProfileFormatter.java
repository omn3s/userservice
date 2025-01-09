package org.omn3s.userservice.web;

import org.omn3s.userservice.user.User;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class ProfileFormatter {
    public static final String EMAIL = Credentials.EMAIL_FIELD;
    public static final String REGISTERED = "registered";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());


    public Map<String, Object> format(User user) {
        // Only include relevant information in response.
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        String datetimeString = dateFormatter.format(user.registered());
        fields.put(EMAIL, user.email());
        fields.put(REGISTERED, datetimeString);
        return fields;
    }
}
