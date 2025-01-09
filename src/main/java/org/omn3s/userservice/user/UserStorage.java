package org.omn3s.userservice.user;

import org.omn3s.userservice.utils.StorageException;

import java.util.List;
import java.util.Optional;

/**
 * Defines contract for storage of user data
 */
public interface UserStorage {
    void initialise() throws StorageException;

    User create(User user) throws StorageException;

    Optional<User> findByEmail(String email) throws StorageException;

    List<User> findAll() throws StorageException;
}
