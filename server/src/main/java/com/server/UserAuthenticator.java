package com.server;

import java.sql.SQLException;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.BasicAuthenticator;

public class UserAuthenticator extends BasicAuthenticator {

    private final UserDB db;

    public UserAuthenticator(String realm) {

        super(realm);
        db = UserDB.getInstance();
    }

    /**
     * Checks the credentials of a user by verifying the provided username and
     * password against the database records.
     *
     * @param username The username to check for credentials.
     * @param password The password to check for credentials.
     * @return boolean True if the provided credentials are valid, false otherwise.
     * @throws SQLException If a database access error occurs or the SQL statement
     *                      is not valid.
     */

    @Override
    public boolean checkCredentials(String username, String password) {

        boolean validCredentials;
        System.out.println("Checking user: " + username + " credentials");
        try {
            validCredentials = db.authenticateUser(username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return validCredentials;
    }

    /**
     * Registers a new user in the database with the provided username, password,
     * email, and user nickname.
     *
     * @param username The desired username for the new user.
     * @param password The password for the new user.
     * @param email    The email address for the new user.
     * @param nickname The user nickname for the new user.
     * @return True if the user is successfully registered, false otherwise (e.g.,
     *         if the username already exists
     *         or if the required fields are missing).
     * @throws JSONException If there is an issue with creating the JSONObject for
     *                       user information.
     * @throws SQLException  If a database access error occurs or the SQL statement
     *                       is not valid.
     */
    public boolean addUser(String username, String password, String email, String nickname)
            throws JSONException, SQLException {

        boolean result = db.setUser(new JSONObject().put("username", username).put("password", password)
                .put("email", email).put("userNickname", nickname));
        if (!result) {
            System.out.println("Registering user " + username + " failed");
            return false;
        }

        System.out.println("User " + username + " registered");
        return true;
    }
}
