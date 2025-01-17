package org.omn3s.userservice;

import io.javalin.testtools.HttpClient;
import io.javalin.testtools.JavalinTest;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.omn3s.userservice.user.User;
import org.omn3s.userservice.utils.EncodedPassword;
import org.omn3s.userservice.utils.NativeKey;
import org.omn3s.userservice.web.*;

import java.net.HttpURLConnection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ApplicationFunctionalTests {


    public static final String EMAIL = "fred@bloggs.com";
    public static final String EMAIL2 = "fred@example.com";
    public static final String PASSWORD = "Foobar!!";

    public ApplicationFunctionalTests() {
    }


    @Test
    void testRegisterUser() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                assertSingleUser(application);
            });
        }
    }

    @Test
    void testRegisterUserNotValidEmail() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                Response signup = signup(application, client, "example.com", PASSWORD);
                Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, signup.code());
            });
        }
    }

    @Test
    void testRegisterUserNotValidPassword() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                Response signup = signup(application, client, EMAIL, "foo");
                Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, signup.code());
            });
        }
    }

    @Test
    void testRegisterUserTwice() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                Response failedResponse = client.post(APIPaths.REGISTER, new Credentials(EMAIL, PASSWORD));
                Assertions.assertEquals(HttpURLConnection.HTTP_CONFLICT, failedResponse.code());
                assertSingleUser(application);
            });
        }
    }

    @Test
    void testLoginOK() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                Response loginResponse = client.post(APIPaths.LOGIN, new Credentials(EMAIL, PASSWORD));
                Assertions.assertEquals(HttpURLConnection.HTTP_OK, loginResponse.code());
                String string = loginResponse.body().string();
                System.err.println(string);
            });
        }
    }

    @Test
    void testLoginBadPassword() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, "PASSWORD");
                String token = loginAndGetToken(application, client);
                Assertions.assertNull(token);
            });
        }
    }

    @Test
    void testFormLoginPassword() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                String formBody = "email=%s&password=%s".formatted(EMAIL, PASSWORD);
                MediaType contentType = MediaType.get("application/x-www-form-urlencoded");
                Response response = client.request(APIPaths.LOGIN,
                        (req) -> req.post(RequestBody.create(formBody, contentType))
                );
                Assertions.assertEquals(HttpURLConnection.HTTP_OK, response.code());
            });
        }
    }

    @Test
    void testLoginNoEmailInForm() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                String formBody = "email2=%s&password=%s".formatted(EMAIL, PASSWORD);
                MediaType contentType = MediaType.get("application/x-www-form-urlencoded");
                Response response = client.request(APIPaths.LOGIN,
                        (req) -> req.post(RequestBody.create(formBody, contentType))
                );
                Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            });
        }
    }

    @Test
    void testLoginNoForm() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                String formBody = "email2=%s&password=%s".formatted(EMAIL, PASSWORD);
                MediaType contentType = MediaType.get("application/x-www-form-urlencoded");
                Response response = client.request(APIPaths.LOGIN,
                        (req) -> req.post(RequestBody.create(formBody, null))
                );
                Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            });
        }
    }

    @Test
    void testLoginFormNoEmailInForm() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                String formBody = "email2=%s&password=%s".formatted(EMAIL, PASSWORD);
                MediaType contentType = MediaType.get("application/x-www-form-urlencoded");
                Response response = client.request(APIPaths.LOGIN,
                        (req) -> req.post(RequestBody.create(formBody, contentType))
                );
                Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, response.code());
            });
        }
    }

    @Test
    void testLoginNoEmailJson() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                Map<String, Object> json = new LinkedHashMap<>();
                json.put(Credentials.EMAIL_FIELD, EMAIL);
                json.put(Credentials.PASSWORD_FIELD, PASSWORD);
                Assertions.assertEquals(HttpURLConnection.HTTP_OK,
                        client.post(APIPaths.LOGIN, json).code());
                json.remove(Credentials.EMAIL_FIELD);
                Assertions.assertEquals(HttpURLConnection.HTTP_BAD_REQUEST,
                        client.post(APIPaths.LOGIN, json).code());

            });
        }
    }


    @Test
    void testLoginNoUser() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                String token = loginAndGetToken(application, client);
                Assertions.assertNull(token);
            });
        }
    }

    @Test
    void testFullWalkthrough() {
        String expected = createProfileBody();

        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                String token = loginAndGetToken(application, client);
                Assertions.assertNotNull(token);
                Response profile = getProfile(application, client, token);
                Assertions.assertEquals(HttpURLConnection.HTTP_OK, profile.code());
                String body = profile.body().string();
                Assertions.assertEquals(expected, body);
            });
        }
    }

    @NotNull
    private static String createProfileBody() {
        ProfileFormatter formater = new ProfileFormatter();
        User user = new User(NativeKey.newId(), EMAIL, new EncodedPassword(PASSWORD), Instant.now());
        Map<String, Object> profileMap = formater.format(user);
        return "{\"email\":\"%s\",\"registered\":\"%s\"}"
                .formatted(EMAIL, profileMap.get(ProfileFormatter.REGISTERED));
    }

    @Test
    void testFullWalkthroughBadToken() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                Response profile = getProfile(application, client, "foobar");
                Assertions.assertEquals(HttpURLConnection.HTTP_FORBIDDEN, profile.code());
            });
        }
    }


    @Test
    void testFullWalkthroughExpiredToken() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                Instant christmasDay = Instant.parse("2024-12-25T00:00:00.00Z");
                String expired = application.getAccessManager()
                        .makeToken(EMAIL, Role.AUTHENTICATED, christmasDay, 1, TimeUnit.HOURS);
                Response profile = getProfile(application, client, expired);
                Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, profile.code());
            });
        }
    }

    @Test
    void testValidTokenByNoUser() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                String nouser = application.getAccessManager()
                        .makeToken(EMAIL2, Role.AUTHENTICATED, Instant.now(), 1, TimeUnit.HOURS);
                String positiveControl = application.getAccessManager()
                        .makeToken(EMAIL, Role.AUTHENTICATED, Instant.now(), 1, TimeUnit.HOURS);

                Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
                        getProfile(application, client, nouser).code());

                Assertions.assertEquals(HttpURLConnection.HTTP_OK,
                        getProfile(application, client, positiveControl).code());
            });
        }
    }


    @Test
    void testLoginLimit() {
        try (Application application = new Application()) {
            JavalinTest.test(application.getApp(), (server, client) -> {
                signup(application, client, PASSWORD);
                String token = loginAndGetToken(application, client);
                Assertions.assertNotNull(token);
                int tries = 0;
                while (tries < 20) {
                    tries++;
                    Response loginResponse = client.post(APIPaths.LOGIN, new Credentials(EMAIL, PASSWORD));
                    int status = loginResponse.code();
                    if (status == 429) {
                        break;
                    } else {
                        Assertions.assertEquals(HttpURLConnection.HTTP_OK, status);
                    }
                }
                // Due to timing issues 4 requests could occur in minute X and 5 could occur in minute X+1
                // So maximum sequence is 9 requests.
                Assertions.assertTrue(tries >= 5 && tries < 10);
            });

        }
    }


    private static void signup(Application application, HttpClient client, String password) {
        Response signup = signup(application, client, EMAIL, password);
        Assertions.assertEquals(HttpURLConnection.HTTP_CREATED, signup.code());
    }

    private static Response signup(Application application, HttpClient client, String email, String password) {
        Credentials user = new Credentials(email, password);
        return client.post(APIPaths.REGISTER, user);

    }

    private static String loginAndGetToken(Application application, HttpClient client) throws Exception {
        Response loginResponse = client.post(APIPaths.LOGIN, new Credentials(EMAIL, PASSWORD));
        if (loginResponse.code() == HttpURLConnection.HTTP_OK) {
            return loginResponse.body().string();
        } else {
            Assertions.assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, loginResponse.code());
            return null;
        }
    }

    private static Response getProfile(Application application, HttpClient client, String token) {
        String headerValue = AccessManager.SCHEME + " " + token;
        return client.get(APIPaths.USER_PROFILE, (req) ->
                req.header(AccessManager.AUTHORIZATION, headerValue)
        );
    }


    private static void assertSingleUser(Application application) {
        List<User> users = application.getUserService().findAll();
        Assertions.assertEquals(1, users.size());
        Assertions.assertEquals(EMAIL, users.get(0).email());
        Assertions.assertNotEquals(PASSWORD, users.get(0).password().encoded());
    }


}