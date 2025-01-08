package org.omn3s.userservice;

import io.javalin.Javalin;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.omn3s.userservice.utils.PasswordManager;
import org.omn3s.userservice.user.UserSQLStorage;
import org.omn3s.userservice.user.UserService;
import org.omn3s.userservice.user.UserStorage;
import org.omn3s.userservice.web.AccessManager;
import org.omn3s.userservice.web.TokenSupport;
import org.omn3s.userservice.web.UserAPI;

import java.io.Closeable;
import java.io.IOException;

public class Application implements Closeable {

    public static void main(String[] args) throws Exception {
        new Application().start();
    }


    private final Javalin app;
    private final UserService userService;
    private final AccessManager accessManager;
    private int port;
    /*
     * When using an in-memory database, the database is lost when the last connection closes.
     * This field holds a connection to the database to ensure the data has the same life span as
     * the Application instance
     */
    private Handle memoryDBReference;

    public Application() throws Exception {
        this(7070, "jdbc:h2:mem:users");
    }

    public Application(int port, String dburl) throws IOException {
        setPort(port);
        Jdbi jdbi = Jdbi.create(dburl);
        memoryDBReference = jdbi.open();
        UserStorage storage = new UserSQLStorage(jdbi);
        storage.initialise();
        PasswordManager passwords = new PasswordManager();
        userService = new UserService(storage, passwords);

        // Configure Web Access
        app = Javalin.create(/*config*/);
        UserAPI userAPI = new UserAPI(userService);
        TokenSupport tokenSupport = TokenSupport.random();
        accessManager = new AccessManager(userAPI, tokenSupport);
        accessManager.install(app);
        userAPI.install(app);
    }

    public Javalin getApp() {
        return app;
    }

    public AccessManager getAccessManager() {
        return accessManager;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }


    public void start() throws Exception {
        app.start(getPort());
    }

    @Override
    public void close() {
        app.stop();
        memoryDBReference.close();
        memoryDBReference = null;
    }
}