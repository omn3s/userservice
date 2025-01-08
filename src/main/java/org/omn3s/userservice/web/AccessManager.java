package org.omn3s.userservice.web;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.ForbiddenResponse;
import io.javalin.http.UnauthorizedResponse;
import io.javalin.http.util.NaiveRateLimit;
import io.javalin.security.RouteRole;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AccessManager {

    public static final String AUTHORIZATION = "Authorization";
    public static final String SCHEME = "Bearer";


    private final Authenticator authenticator;
    private final TokenSupport tokenSupport;

    public AccessManager(Authenticator authenticator, TokenSupport tokenSupport) {
        this.authenticator = authenticator;
        this.tokenSupport = tokenSupport;
    }

    public void install(Javalin app) {
        app.beforeMatched(this::checkAccess);
        app.post(APIPaths.LOGIN, this::login, Role.ANYONE);
    }

    public void checkAccess(Context ctx) {
        Set<RouteRole> permittedRoles = ctx.routeRoles();
        if (permittedRoles.contains(Role.ANYONE)) return;
        TokenSupport.DecodeResult decodedToken = getDecodeToken(ctx);
        // Not authentication provided
        if (decodedToken == null || decodedToken.expired()) {
            throw new UnauthorizedResponse();
        }
        if (!permittedRoles.contains(decodedToken.role())) {
            throw new ForbiddenResponse();
        }
        Authenticator.setEmail(ctx, decodedToken.email());
    }

    public String makeToken(String email, Role role, Instant when, int duration, TimeUnit timeUnit) {
        return tokenSupport.createToken(email, role, when, duration, timeUnit);
    }

    private TokenSupport.DecodeResult getDecodeToken(Context ctx) {
        // First Check authorisation?
        String authorization = ctx.header(AUTHORIZATION);
        String token = null;
        if (authorization != null && authorization.startsWith(SCHEME)) {
            token = authorization.substring(SCHEME.length()).trim();
            return tokenSupport.decode(token);
        }
        return null;
    }


    public void login(Context ctx) throws Exception {
        NaiveRateLimit.requestPerTimeUnit(ctx, 10, TimeUnit.MINUTES); // throws if rate limit is exceeded
        Credentials credentials = Credentials.extractCredentials(ctx);
        String email = credentials.getEmail();
        String password = credentials.getPassword();
        if (email == null || password == null)
            throw new BadRequestResponse();
        if (!authenticator.authenticate(email, password)) {
            throw new UnauthorizedResponse();
        }
        String token = tokenSupport.createToken(email, Role.AUTHENTICATED);
        ctx.json(token);
    }
}
