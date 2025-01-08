package org.omn3s.userservice.user;

import org.eclipse.jetty.util.IO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Defines contract for storage of user data
 */
public interface UserStorage {
    void initialise() throws IOException;

    User create(User user) throws IOException;

    Optional<User> findByEmail(String email) throws IOException;

    List<User> findAll() throws IOException;
}
