package com.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.json.JSONArray;
import org.json.JSONObject;

public class MessageDatabase {

    private Connection dbConnection = null;
    private static MessageDatabase dbInstance = null;
    private static final UserDB userDB = UserDB.getInstance();

    public static synchronized MessageDatabase getInstance() {

        if (dbInstance == null) {
            dbInstance = new MessageDatabase();
        }
        return dbInstance;
    }

    private MessageDatabase() {
        try {
            open("MyMessageDatabase.db");
            ;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializes the database
     * 
     * @return boolean true if database creation succeeds, otherwise false
     * @throws SQLException If a database access error occurs or the given SQL
     *                      statement is not valid.
     */
    private boolean initializeDatabase() throws SQLException {

        if (dbConnection != null) {
            String createMessageDB = "create table message (locationName varchar(500) NOT NULL, locationDescription varchar(500) NOT NULL, locationCity varchar(500) NOT NULL, locationCountry varchar(100), locationAddress varchar(100), originalPoster varchar(100) NOT NULL,"
                    + " originalPostingTime integer, latitude varchar(500), longitude varchar(500))";

            Statement createStatement = dbConnection.createStatement();
            createStatement.executeUpdate(createMessageDB);
            createStatement.close();

            System.out.println("Database created succesfully");

            return true;
        }

        System.out.println("Database creation failed");
        return false;
    }

    public void closeDB() throws SQLException {

        if (dbConnection != null) {
            dbConnection.close();
            dbConnection = null;
            System.out.println("Closing database connection");
        }
    }

    /**
     * Sets a user message in the database based on the information provided in a
     * JSONObject.
     *
     * @param message The JSONObject containing information about the user message,
     *                including location details,
     *                original poster, posting time, and coordinates.
     * @throws SQLException If a database access error occurs or the given SQL
     *                      statement is not valid.
     */
    public void setMessage(JSONObject message, String username) throws SQLException {
        System.out.println("Entering setMessage");
        String location = message.getString("locationName");
        String description = message.getString("locationDescription");
        String city = message.getString("locationCity");
        String country = message.getString("locationCountry");
        String address = message.getString("locationStreetAddress");
        String originalPoster = userDB.getUserNickName(username);

        String postingTime = message.getString("originalPostingTime");
        String latitude = message.optString("latitude");
        String longitude = message.optString("longitude");

        UserMessage um = new UserMessage(location, description, city, country, address, originalPoster, postingTime,
                latitude, longitude);
        long sentLong = um.dateAsInt();
        System.out.println("AIKA UNIX AIKANA: " + sentLong);

        System.out.println("Time parsed");
        String setMessageString = "insert into message " + "VALUES('" + location + "','" + description + "','" +
                city + "','" + country + "','" + address + "','" + originalPoster + "','" + sentLong + "','" + latitude
                + "','" +
                longitude + "')";

        Statement createStatement;
        createStatement = dbConnection.createStatement();
        createStatement.executeUpdate(setMessageString);
        createStatement.close();
    }

    /**
     * Retrieves messages from the database and returns them in a JSONArray.
     *
     * @return A JSONArray containing messages with location details, posting time
     *         etc.
     * @throws SQLException If a database access error occurs or the SQL statement
     *                      is not valid.
     */
    public JSONArray getMessages() throws SQLException {
        
        System.out.println("ENTERING GET MESSAGE... \n");
        Statement queryStatement = null;
        JSONArray array = new JSONArray();

        String getMessagesString = "select locationName, locationDescription, locationCity," +
        " locationCountry, locationAddress, originalPoster, originalPostingTime," +
        " longitude, latitude from message";
        queryStatement = dbConnection.createStatement();
        ResultSet rs = queryStatement.executeQuery(getMessagesString);

        // Putting message in JSONArray
        while (rs.next()) {
            JSONObject obj = new JSONObject();
            ZonedDateTime postTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(rs.getLong("originalPostingTime")),
                    ZoneOffset.UTC);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
            System.out.println("original poster in resultset is: " + rs.getString("originalPoster"));
            obj.put("locationName", rs.getString("locationName"));
            obj.put("locationDescription", rs.getString("locationDescription"));
            obj.put("locationCity", rs.getString("locationCity"));
            obj.put("locationCountry", rs.getString("locationCountry"));
            obj.put("locationStreetAddress", rs.getString("locationAddress"));
            
            obj.put("originalPoster", rs.getString("originalPoster"));
            obj.put("originalPostingTime", postTime.format(formatter));

            // Latitude and longitude only inserted to JSONArray if they exist.
            System.out.println("RS GET STRING: " + rs.getString("latitude"));
            if (!rs.getString("latitude").isEmpty() && !rs.getString("longitude").isEmpty()) {
                obj.put("latitude", rs.getDouble("latitude"));
                obj.put("longitude", rs.getDouble("longitude"));
            }

            array.put(obj);
        }
        System.out.println("JSON ARRAY TO STIRNG: " + "\n\n" + array.toString());
        return array;
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
