package org.omn3s.userservice.user;

import org.eclipse.jetty.util.IO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserStorage {
    void initialise() throws IOException;

    public User create(User user) throws IOException;

    public Optional<User> findByEmail(String email) throws IOException;

    List<User> findAll() throws IOException;
}
