package org.omn3s.userservice.web;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Support class for the generation and validation of authentication tokens.
 */
public class TokenSupport {


    private static final Logger log = LoggerFactory.getLogger(TokenSupport.class);

    public record DecodeResult(String email, Role role, boolean valid, boolean expired) {
        public DecodeResult(String email, Role role) {
            this(email, role, true, false);
        }
    }

    public static final DecodeResult EXPIRED = new DecodeResult(null, Role.NONE, false, true);
    public static final DecodeResult BAD_FORMAT = new DecodeResult(null, Role.NONE, false, false);

    public static final String ROLENAME = "role";
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public TokenSupport(Algorithm algorithm) {
        this.algorithm = algorithm;
        this.verifier = JWT.require(algorithm).build();
    }

    public String createToken(String email, Role role) {
        return createToken(email, role, Instant.now(), 1, TimeUnit.HOURS);
    }

    public String createToken(String email, Role role, Instant from, int duration, TimeUnit timeUnit) {
        return JWT.create()
                .withIssuedAt(from)
                .withSubject(email)
                .withClaim(ROLENAME, role.name())
                .withExpiresAt(from.plusMillis(timeUnit.toMillis(duration)))
                .sign(algorithm);
    }

    public DecodeResult decode(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);
            String email = decoded.getSubject();
            String rolename = decoded.getClaim(ROLENAME).asString();
            Role role = Role.valueOf(rolename.toUpperCase());
            return new DecodeResult(email, role);
        } catch (TokenExpiredException expiredException) {
            return EXPIRED;
        } catch (Exception parsingFailure) {
            log.warn("Failure parsing token: %s", parsingFailure.getMessage());
            return BAD_FORMAT; // Bad Format
        }
    }

    public static TokenSupport withRandomSecret() {
        byte[] secret = new byte[256];
        new SecureRandom().nextBytes(secret);
        return new TokenSupport(Algorithm.HMAC512(secret));
    }
}
