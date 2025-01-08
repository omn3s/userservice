package org.omn3s.userservice.web;

import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;

/**
 * Wrapper class containing email/ password pair
 */
public class Credentials {
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    private String email;
    private String password;

    public Credentials(String email, String password) {
        setEmail(email);
        setPassword(password);
    }

    public Credentials() {
    }

    public static Credentials extractCredentials(Context context)  {
        boolean json = context.isJson();
        boolean form = context.isFormUrlencoded();
        Credentials credentials;
        if (json) {
            // Check for json
            credentials = context.bodyAsClass(Credentials.class);
        } else if (form) {
            credentials = new Credentials(
                    context.formParam(EMAIL),
                    context.formParam(PASSWORD)
            );
        } else {
            throw new BadRequestResponse("No parameters found");
        }
        return credentials;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
