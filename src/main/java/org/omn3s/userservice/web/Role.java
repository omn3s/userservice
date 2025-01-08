package org.omn3s.userservice.web;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    NONE,
    ANYONE,
    AUTHENTICATED
}
