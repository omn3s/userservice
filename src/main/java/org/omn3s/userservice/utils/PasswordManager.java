package org.omn3s.userservice.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * This class implements behaviour regarding encoding/validation of password and implementing a
 * basic password restriction policy.
 */
public class PasswordManager {
    private int minLength;

    public PasswordManager() {
        setMinLength(6);
    }

    public int getMinLength() {
        return minLength;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public EncodedPassword encode(String cleartext) {
        return new EncodedPassword(
                BCrypt.hashpw(cleartext, BCrypt.gensalt()));
    }

    public boolean validate(String cleartext, EncodedPassword encoded) {
        return BCrypt.checkpw(cleartext, encoded.encoded());
    }

    public boolean isValid(String password) {
        return (password.length() >= getMinLength());
    }
}
