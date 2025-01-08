package org.omn3s.userservice.web;

import io.javalin.http.Context;

import java.io.IOException;

/**
 * Interface defining method to validate the username / password combination
 */
public interface Authenticator {
    String EMAIL = "email";

    boolean authenticate(String username, String password) throws IOException;


    static String getEmail(Context context) {
        return context.attribute(EMAIL);
    }

    static void setEmail(Context context, String email) {
        context.attribute(EMAIL, email);
    }
}
