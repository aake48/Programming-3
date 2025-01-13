package com.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;
import java.security.SecureRandom;
import org.apache.commons.codec.digest.Crypt;

import org.json.JSONObject;


public class UserDB {
    private Connection dbConnection = null;
    private static UserDB dbInstance = null;
    private SecureRandom secureRandom = new SecureRandom();

    public static synchronized UserDB getInstance() {
        if (dbInstance == null) {
            dbInstance = new UserDB();
        }
        return dbInstance;
    }

    UserDB() {
        try {
            open("MyUserDatabase.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the database
     * 
     * @return boolean true if initialization succeeds, otherwise false
     * @throws SQLException If a database access error occurs or the given SQL
     *                      statement is not valid.
     */
    private boolean initializeDatabase() throws SQLException {

        if (dbConnection != null) {
            String createUserTable = "create table users (username varchar(50) NOT NULL, password varchar(50) NOT NULL, email varchar(50), userNickname varchar(50), primary key(username))";
            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createUserTable);
            createStatement.close();

            System.out.println("Database created succesfully");

            return true;
        }

        System.out.println("Failed to create database");
        return false;

    }

    public void closeDB() throws SQLException {
        if (dbConnection != null) {
            dbConnection.close();
            dbConnection = null;
            System.out.println("Database connection closed");
        }
    }

    /**
     * Sets user information in the database based on the provided JSONObject.
     *
     * @param user The JSONObject containing username,
     *             password, email, and user nickname.
     * @return True if the user is successfully set in the database, false otherwise
     *         (e.g., if the username already exists
     *         or if the required fields are missing).
     * @throws SQLException If a database access error occurs or the SQL statement
     *                      is not valid.
     */

    public boolean setUser(JSONObject user) throws SQLException {
        if (checkIfUserExists(user.getString("username"))) {
            return false;
        }

        if (!user.has("password") || !user.has("userNickname") || !user.has("email") || !user.has("username")) {
            return false;
        }
        byte[] bytes = new byte[13];
        secureRandom.nextBytes(bytes);
        String saltBytes = new String(Base64.getEncoder().encode(bytes));
        String salt = "$6$" + saltBytes;

        String hashedPassword = Crypt.crypt(user.getString("password"), salt);
        String setUserString = "insert into users " + "VALUES('" +
                user.getString("username") + "','" + hashedPassword + "','" +
                user.getString("email") + "','" + user.getString("userNickname") + "')";

        Statement createStatement;
        createStatement = dbConnection.createStatement();
        createStatement.executeUpdate(setUserString);
        createStatement.close();
        return true;

    }

    /**
     * Checks if the user exists in the database
     * 
     * @param givenUserName The username to check for existence
     * @return boolean true if user exists, otherwise false
     * @throws SQLException If a database access error occurs or the SQL statement
     *                      is not valid.
     */
    public boolean checkIfUserExists(String givenUserName) throws SQLException {

        Statement queryStatement = null;
        ResultSet rs;

        String checkUser = "select username from users where username = '" + givenUserName + "'";
        System.out.println("checking user");

        queryStatement = dbConnection.createStatement();
        rs = queryStatement.executeQuery(checkUser);

        if (rs.next()) {
            System.out.println("user exists");
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the users nickname
     * 
     * @param givenUserName The username to check for nickname
     * @return The user nickname
     * @throws SQLException If a database access error occurs or the SQL statement
     *                      is not valid.
     */

    public String getUserNickName(String givenUserName) throws SQLException {

        Statement queryStatement = null;
        ResultSet rs;

        String checkUser = "select userNickname from users where username = '" + givenUserName + "'";
        System.out.println("Getting user's nickname...");

        queryStatement = dbConnection.createStatement();
        rs = queryStatement.executeQuery(checkUser);

        if (rs.next()) {
           return rs.getString("userNickname");
            
        }
        return null;
    }

    /**
     * Authenticates a user by checking the given username and password against the
     * information in the database
     * Hashes and salts the password
     *
     * @param givenUserName The username provided for authentication.
     * @param givenPassword The password provided for authentication.
     * @return boolean True if the authentication is successful, false otherwise.
     * @throws SQLException If a database access error occurs or the SQL statement
     *                      is not valid.
     */
    public boolean authenticateUser(String givenUserName, String givenPassword) throws SQLException {
        Statement queryStatement = null;
        ResultSet rs;
        System.out.println("Authenticating user...");

        String getMessageString = "SELECT username, password FROM users WHERE username = '" + givenUserName + "'";
        System.out.println(givenUserName);

        queryStatement = dbConnection.createStatement();
        rs = queryStatement.executeQuery(getMessageString);
        if (!rs.next()) {   

            System.out.println("User not found");
            return false;
        } else {
            String hashedPassword = rs.getString("password");
            Crypt.crypt(givenPassword, hashedPassword);
            if (hashedPassword.equals(Crypt.crypt(givenPassword, hashedPassword))) {
                return true;
            }

        }

        return false;

    }

    /**
     * Opens a connection to the SQLite database specified by the provided database
     * name, if database doesn't exist, initialize it.
     *
     * @param dbName The name of the SQLite database to be opened.
     * @throws SQLException If a database access error occurs
     */

    public void open(String dbName) throws SQLException {

        boolean fileExists;

        File file = new File(dbName);
        fileExists = file.exists();

        String database = "jdbc:sqlite:" + dbName;
        dbConnection = DriverManager.getConnection(database);
        if (!fileExists) {
            System.out.println("Database not found, initializing...");
            initializeDatabase();
        }

    }
}