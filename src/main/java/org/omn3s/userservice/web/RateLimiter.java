package org.omn3s.userservice.web;

import io.javalin.http.Context;
import io.javalin.http.HttpResponseException;
import io.javalin.http.HttpStatus;
import org.omn3s.userservice.utils.SimpleRateCounter;

import java.util.Map;

public class RateLimiter {
    private int requestsPerMinute;
    private final SimpleRateCounter counter = new SimpleRateCounter(60 * 1000L);


    public RateLimiter(int requestsPerMinute) {
        setRequestsPerMinute(requestsPerMinute);
    }

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public void setRequestsPerMinute(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }


    public void limit(Context ctx) {
        String userPart = Authenticator.getEmail(ctx);
        String path = ctx.path();
        if (userPart == null)
            userPart = ctx.ip();
        long count = counter.increment(System.currentTimeMillis(), userPart, path);
        if (count > getRequestsPerMinute()) {
            String message = "Rate limit exceeded - Server allows %d requests per minute.".formatted(requestsPerMinute);
            throw new HttpResponseException(HttpStatus.TOO_MANY_REQUESTS, message, Map.of());
        }
    }
}
