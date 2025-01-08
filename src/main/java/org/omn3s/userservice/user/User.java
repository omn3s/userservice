package org.omn3s.userservice.user;

import org.omn3s.userservice.utils.EncodedPassword;
import org.omn3s.userservice.utils.NativeKey;

import java.time.Instant;
import java.util.Objects;

/**
 * Class representing the data stored for a user.
 */
public record User(NativeKey uid, String email,
                   EncodedPassword password,
                   Instant registered, long updated) {
    public User {
        // Validations
        Objects.requireNonNull(email);
        Objects.requireNonNull(password);
        Objects.requireNonNull(registered);
    }
    public long getRegisteredEpochMillis() {
        return registered.toEpochMilli();
    }

}
