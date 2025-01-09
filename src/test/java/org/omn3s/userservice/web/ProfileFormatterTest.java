package org.omn3s.userservice.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omn3s.userservice.user.User;
import org.omn3s.userservice.utils.EncodedPassword;
import org.omn3s.userservice.utils.NativeKey;

import java.time.Instant;
import java.util.Map;

class ProfileFormatterTest {
    @Test
    void testFormatting() {
        User user = new User(NativeKey.newId(), "fred@bloggs.com", new EncodedPassword("buzzz"), Instant.parse("2024-12-25T00:01:00.00Z"));
        ProfileFormatter formatter = new ProfileFormatter();
        Map<String, Object> fields = formatter.format(user);
        Assertions.assertEquals(2, fields.size(), "More than 2 fields included");
        Assertions.assertEquals("fred@bloggs.com", fields.get(ProfileFormatter.EMAIL),
                "Email is incorrect");
        Assertions.assertEquals("2024-12-25", fields.get(ProfileFormatter.REGISTERED),
                "Registration Date is incorrect");
        Assertions.assertEquals("{email=fred@bloggs.com, registered=2024-12-25}", fields.toString());


    }
}