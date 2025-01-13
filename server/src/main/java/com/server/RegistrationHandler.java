package com.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class RegistrationHandler implements HttpHandler {

    private UserAuthenticator userAuth;

    RegistrationHandler(UserAuthenticator ua) {
        this.userAuth = ua;
    }

    /**
     * Handles an HTTP post request, no other requests are supported
     *
     * @param exchange The HTTP exchange object representing the client-server
     *                 communication.
     * @throws IOException If an I/O error occurs during the processing of the HTTP
     *                     request or response.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            // Handle POST requests here (users send this for sending messages)
            handlePOSTRequest(exchange);
        } else {

            String responseString = "Not supported";

            byte[] bytes = responseString.getBytes("UTF-8");
            exchange.sendResponseHeaders(400, bytes.length);

            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(bytes);

            outputStream.flush();
            outputStream.close();
        }

    }

    /**
     * Handles an HTTP POST request by processing the incoming request body,
     * validating the content type,
     * and registering a new user based on the provided credentials.
     *
     * @param exchange The HTTP exchange object representing the client-server
     *                 communication.
     * @throws IOException If an I/O error occurs during the processing of the HTTP
     *                     request or response.
     */
    private void handlePOSTRequest(HttpExchange exchange) throws IOException {

        JSONObject obj = null;
        String contentType = "";
        Headers headers = exchange.getRequestHeaders();
        int code = 200;
        String responseString = "";
        
        try {
            if (headers.containsKey("Content-Type")) {
                contentType = headers.get("Content-Type").get(0);
                System.out.println("Content-Type available");
            } else {
                System.out.println("No content type");
                code = 411;
                responseString = "No content type in request";
            }
            if (contentType.equalsIgnoreCase("application/json")) {
                System.out.println("Content-Type JSON detected");
                InputStream stream = exchange.getRequestBody();
                String newUser = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("\n"));
                stream.close();

                if (newUser == null || newUser.length() == 0) {

                    code = 412;
                    responseString = "No user credentials";
                } else {
                    try {
                        obj = new JSONObject(newUser);
                    } catch (JSONException e) {
                        System.out.println("JSON parse error");
                    }

                    if (obj.getString("username").length() == 0 || obj.getString("password").length() == 0) {
                        code = 413;
                        responseString = "No proper user credentials";
                    } else {
                        if (!userAuth.addUser(obj.getString("username"), obj.getString("password"),
                                obj.getString("email"), obj.getString("userNickname"))) {
                            code = 405;
                            responseString = "This user already exists";
                        } else {
                            code = 200;
                            responseString = "User registered succesfully";
                        }
                    }
                }

                byte[] bytes = responseString.getBytes("UTF-8");
                exchange.sendResponseHeaders(code, bytes.length);

                OutputStream outputStream = exchange.getResponseBody();
                outputStream.write(bytes);
                outputStream.flush();
                outputStream.close();
            } else {
                code = 407;
                responseString = "Wrong content type";
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}