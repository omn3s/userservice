package org.omn3s.userservice.user;

import org.omn3s.userservice.utils.EmailValidator;
import org.omn3s.userservice.utils.EncodedPassword;
import org.omn3s.userservice.utils.PasswordManager;
import org.omn3s.userservice.utils.StorageException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Class controlling the business logic of user functionality
 */
public class UserService {
    private final UserStorage storage;
    private final PasswordManager passwordManager;
    private final EmailValidator emailValidator = new EmailValidator();

    public UserService(UserStorage storage,
                       PasswordManager passwordManager
    ) {
        this.storage = storage;
        this.passwordManager = passwordManager;
    }

    public void register(String email, String password) throws StorageException {
        boolean emailValid = emailValidator.isValid(email);
        boolean passwordOK = passwordManager.isValid(password);
        if (passwordOK && emailValid) {
            Instant now = Instant.now();
            EncodedPassword encoded = passwordManager.encode(password);
            User user = new User(null, email, encoded, now);
            storage.create(user);
        } else {
            String message = "Errors ";
            if (!emailValid) message += "\nEmail address is not valid";
            if (!passwordOK) message += "\nPassword does not meet requirements";
            throw new IllegalArgumentException(message);
        }
    }

    public Optional<User> findByEmail(String email) throws StorageException {
        return storage.findByEmail(email);
    }

    public boolean authenticate(String email, String password) throws StorageException {
        Optional<User> byEmail = findByEmail(email);
        return byEmail.filter(
                        (u) -> passwordManager.validate(password, u.password()))
                .isPresent();
    }


    public List<User> findAll() throws StorageException {
        return storage.findAll();
    }
}
