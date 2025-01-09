package org.omn3s.userservice.user;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.omn3s.userservice.utils.DuplicateEntityException;
import org.omn3s.userservice.utils.EncodedPassword;
import org.omn3s.userservice.utils.NativeKey;
import org.omn3s.userservice.utils.StorageException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserStorage with an SQL based backend
 */
public class UserSQLStorage implements UserStorage {
    public static final String EMAIL = "email";
    public static final String HASHPW = "hashpw";
    public static final String UID = "uid";
    public static final String CREATED = "registered";

    private final Jdbi database;
    private final String[] initialisation = {
            "CREATE TABLE IF NOT EXISTS USERS (uid CHAR(40) PRIMARY KEY, email VARCHAR(255) UNIQUE, hashpw VARCHAR(255) , registered bigint)"
    };

    private String select = "SELECT uid , email, hashpw , registered from USERS";

    private String create =
            " insert into USERS(uid , email, hashpw , registered) VALUES(?,?,?,?)";

    public UserSQLStorage(Jdbi database) {
        this.database = database;
        database.registerRowMapper(User.class, new UserMapper());
    }

    public void initialise() throws StorageException {
        try (Handle handle = database.open()) {
            for (String statement : initialisation) {
                handle.execute(statement);
            }
        } catch (Exception failed) {
            // Wrap exception to higher level handling
            throw new StorageException("Initialisation Failure", failed);
        }
    }


    public User create(User user) throws DuplicateEntityException, StorageException {
        NativeKey nativeKey = NativeKey.newId();
        try (Handle handle = database.open()) {
            handle.execute(create, nativeKey.representation(), user.email(), user.password().encoded(), user.getRegisteredEpochMillis());
        } catch (Exception failure) {
            // Causes of failure could be networking or integrity constraints
            // Check that User not registered
            if (findByEmail(user.email()).isPresent())
                throw new DuplicateEntityException("Email already registered", failure);
            throw new StorageException("Insertion Failure", failure);
        }
        return findByEmail(user.email()).get();
    }

    @Override
    public Optional<User> findByEmail(String email) throws StorageException {
        List<User> users = findUsersByEmail(email);
        if (users.size() == 1) {
            return Optional.of(users.get(0));
        } else {
            return Optional.empty();
        }
    }

    public List<User> findUsersByEmail(String email) throws StorageException {
        return selectUsers(select + " where email = ?", email);
    }

    @Override
    public List<User> findAll() throws StorageException {
        return selectUsers(select);
    }

    protected List<User> selectUsers(String query, Object... args) throws StorageException {
        try (Handle handle = database.open()) {
            return handle.select(query, args)
                    .mapTo(User.class)
                    .list();
        } catch (Exception failed) {
            // Wrap exception to higher level handling
            throw new StorageException("Query Failure", failed);
        }
    }

    public static class UserMapper implements RowMapper<User> {
        @Override
        public User map(ResultSet rs, StatementContext ctx) throws SQLException {
            String email = rs.getString(EMAIL);
            String encoded = rs.getString(HASHPW);
            String uid = rs.getString(UID);
            long created = rs.getLong(CREATED);

            return new User(new NativeKey(uid), email, new EncodedPassword(encoded),
                    Instant.ofEpochSecond(created));
        }
    }
}
