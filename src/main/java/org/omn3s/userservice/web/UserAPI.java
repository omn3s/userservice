package org.omn3s.userservice.web;

import io.javalin.Javalin;
import io.javalin.http.*;
import org.jetbrains.annotations.NotNull;
import org.omn3s.userservice.utils.DuplicateEntityException;
import org.omn3s.userservice.user.User;
import org.omn3s.userservice.user.UserService;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Optional;

@SuppressWarnings("rawtypes")
public class UserAPI implements Authenticator {

    private final UserService userService;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-mm-dd")
            .withZone(ZoneId.systemDefault());


    public UserAPI(UserService userService) {
        this.userService = userService;
    }

    public void install(Javalin app) {
        app.post(APIPaths.REGISTER, this::register, Role.ANYONE);
        app.get(APIPaths.USER_PROFILE, this::getProfile, Role.AUTHENTICATED);
    }

    private void register(@NotNull Context context) throws Exception {
        Credentials credentials = Credentials.extractCredentials(context);
        try {
            userService.register(credentials.getEmail(), credentials.getPassword());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestResponse(ex.getMessage());
        } catch (DuplicateEntityException ex) {
            throw new ConflictResponse("Email already registered");
        }
        context.status(HttpStatus.CREATED);
    }

    private void getProfile(@NotNull Context context) throws Exception {
        LinkedHashMap<String, Object> fields = new LinkedHashMap<>();
        String email = Authenticator.getEmail(context);
        Optional<User> maybe = userService.findByEmail(email);
        if (maybe.isEmpty())
            throw new NotFoundResponse();
        User user = maybe.get();
        // Only include relevant information in response.
        fields.put("email", user.email());
        String datetimeString = dateFormatter.format(user.registered());
        fields.put("registered", datetimeString);
        context.json(fields);
    }


    @Override
    public boolean authenticate(String username, String password) throws IOException {
        return userService.authenticate(username, password);
    }


}
