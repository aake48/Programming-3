package com.server;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class MyHandler implements HttpHandler {

    MessageDatabase db = MessageDatabase.getInstance();

    /**
     * Handles an HTTP GET request by retrieving messages from the database and
     * sending the response.
     *
     * @param httpExchange The HTTP exchange object representing the client-server
     *                     communication
     * @throws IOException If an I/O error occurs during the processing of the HTTP
     *                     request or response.
     */
    @Override
    public void handle(HttpExchange t) throws IOException {
        HttpExchange exchange = t;
        // implement GET and POST handling
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            System.out.println("Entering post handler");
            // Handle POST requests here (users send this for sending messages)
            handlePOSTRequest(exchange);
        } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            // Handle GET requests here (users use this to get messages)
            System.out.println("Entering get handler");
            handleGETRequest(exchange);
        } else {
            // Inform user here that only POST and GET functions are supported and send an
            // error code
            // 400 with a message “Not supported” (witouth the “).
            handleRequest(exchange);
        }

    }

    /**
     * Handles an HTTP GET request by retrieving messages from the database and
     * sending the response.
     *
     * @param httpExchange The HTTP exchange object representing the client-server
     *                     communication.
     * @throws IOException If an I/O error occurs during the processing of the HTTP
     *                     request or response.
     */
    private void handleGETRequest(HttpExchange httpExchange) throws IOException {

        String responseString = "";
        try {
            JSONArray responseMessages = new JSONArray();
            responseMessages = db.getMessages();
            if (responseMessages.isEmpty()) {
                httpExchange.sendResponseHeaders(204, -1);
                return;
            }
            responseString = responseMessages.toString();
            byte[] bytes = responseString.getBytes("UTF-8");
            httpExchange.sendResponseHeaders(200, bytes.length);

            OutputStream outputStream = httpExchange.getResponseBody();
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (SQLException e) {
            e.printStackTrace();
            httpExchange.sendResponseHeaders(403, -1);
        }
    }

    /**
     * Handles an HTTP POST request by processing the incoming request body,
     * extracting a JSON object,
     * and storing the message information in the database.
     *
     * @param httpExchange The HTTP exchange object representing the client-server
     *                     communication.
     * @throws IOException            If an I/O error occurs during the processing
     *                                of the HTTP request or response.
     * @throws SQLException           If a database access error occurs or the SQL
     *                                statement is not valid.
     * @throws JSONException          If there is an issue with parsing the incoming
     *                                JSON data.
     * @throws DateTimeParseException If there is an issue with parsing the date and
     *                                time information from the JSON data.
     */
    private void handlePOSTRequest(HttpExchange httpExchange) throws IOException {

        InputStream stream = httpExchange.getRequestBody();
        String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));
        String userNickName = httpExchange.getPrincipal().getUsername();
        try {
            JSONObject obj = new JSONObject(text);
            db.setMessage(obj, userNickName);
            httpExchange.sendResponseHeaders(200, -1);

        } catch (SQLException e) {
            e.printStackTrace();
            httpExchange.sendResponseHeaders(403, -1);
        } catch (JSONException je) {
            je.printStackTrace();
            httpExchange.sendResponseHeaders(403, -1);
        } catch (DateTimeParseException dtpe) {
            dtpe.printStackTrace();
            httpExchange.sendResponseHeaders(403, -1);
        }

    }

    /**
     * Handles an HTTP request with a default response indicating that the request
     * is not supported.
     *
     * @param httpExchange The HTTP exchange object representing the client-server
     *                     communication.
     * @throws IOException If an I/O error occurs during the processing of the HTTP
     *                     request or response.
     */
    private void handleRequest(HttpExchange httpExchange) throws IOException {

        String responseString = "Not supported";

        byte[] bytes = responseString.getBytes("UTF-8");
        httpExchange.sendResponseHeaders(400, bytes.length);

        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(bytes);

        outputStream.flush();
        outputStream.close();
    }

}
