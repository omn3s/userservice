package org.omn3s.userservice.utils;

import java.util.UUID;

/**
 * This is a class  to provide a type-safe facade to primary key implementations.
 * It is assumed that all primary representations can be converted to and from strings.
 *
 * This means if the underlying storage uses ObjectIds or longs or UUID as primary keys
 * that informatioin is localised to the appropriate classes.
 *
 * @param representation
 */
public record NativeKey(String representation) {
    public static NativeKey newId() {
        return new NativeKey(UUID.randomUUID().toString());
    }
}
