package org.omn3s.userservice.web;

import io.javalin.security.RouteRole;

public enum Role implements RouteRole {
    /**
     * Role of any user with or without a valid authentication token
     */
    ANYONE,
    /**
     * Role of any user with a valid authentication token
     */
    AUTHENTICATED,
    /**
     * Role indicating that no access is given
     */
    NONE

}
