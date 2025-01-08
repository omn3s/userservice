package org.omn3s.userservice.user;

import org.omn3s.userservice.utils.EmailValidator;
import org.omn3s.userservice.utils.PasswordManager;
import org.omn3s.userservice.utils.EncodedPassword;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public class UserService {
    public static final String EMAIL = "email";
    private final UserStorage storage;
    private final PasswordManager passwordManager;
    private final EmailValidator emailValidator = new EmailValidator();

    public UserService(UserStorage storage,
                       PasswordManager passwordManager
    ) {
        this.storage = storage;
        this.passwordManager = passwordManager;
    }

    public void register(String email, String password) throws IOException {
        boolean emailValid = emailValidator.isValid(email);
        boolean passwordOK = passwordManager.isValid(password);
        if (passwordOK && emailValid) {
            Instant now = Instant.now();
            EncodedPassword encoded = passwordManager.encode(password);
            User user = new User(null, email, encoded, now, now.toEpochMilli());
            storage.create(user);
        } else {
            String message = "Errors ";
            if (!emailValid) message += "\nEmail address is not valid";
            if (!passwordOK) message += "\nPassword does not meet requirements";
            throw new IllegalArgumentException(message);
        }
    }

    public Optional<User> findByEmail(String email) throws IOException {
        return storage.findByEmail(email);
    }

    public boolean authenticate(String email, String password) throws IOException {
        Optional<User> byEmail = findByEmail(email);
        return byEmail.filter(
                        (u) -> passwordManager.validate(password, u.password()))
                .isPresent();
    }


    public List<User> findAll() throws IOException {
        return storage.findAll();
    }
}
