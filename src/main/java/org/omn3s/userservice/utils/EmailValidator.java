package org.omn3s.userservice.utils;

import java.util.regex.Pattern;

/**
 * Simple Email Validator based on
 * <a href="https://owasp.org/www-community/OWASP_Validation_Regex_Repository">OWASP validation REGEX Repository</a>
 */
public class EmailValidator {
    public static final String REGEXP =
            "^[\\p{L}0-9_+&*-]+(?:\\.[\\p{L}0-9_+&*-]+)*@(?:[\\p{L}0-9-]+\\.)+\\p{L}{2,7}$";
    private final Pattern simpleTest =
            Pattern.compile(REGEXP);

    public boolean isValid(String emailAddress) {
        return simpleTest
                .matcher(emailAddress)
                .matches();
    }
}
