package com.weather.server.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;

import com.weather.http.Request;

public class RequestParser {

    /*
     *  Example format: 
     *  PUT /data/weather HTTP/1.1
        Host: localhost:8080
        Content-Type: application/json
        Content-Length: 53
                                                                    (This is separate by blank line)
        {"location":"London","temperature":22.5,"wind_speed":15}
     * 
     */
    public static Request parse(BufferedReader in) throws IOException {
        String requestLine = in.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            return null;
        }

        String[] parts = requestLine.split(" ");
        if (parts.length < 2) {
            return null; // invalid request line
        }

        String method = parts[0];
        String url = parts[1];

        // Parsing hearders
        HashMap<String, String> headers = new HashMap<>();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            String[] headerParts = line.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }

        // Parsing body of PUT request - because the GET dont have body
        StringBuilder body = new StringBuilder();
        if ("PUT".equalsIgnoreCase(method)) {
            if (headers.containsKey("Content-Length")) {
                int contentLength = Integer.parseInt(headers.get("Content-Length"));
                for (int i = 0; i < contentLength; i++) {
                    body.append((char) in.read());
                }
            } else {
                // If Content-Length is not provided, read until the stream ends or a timeout occurs.
                while (in.ready()) {
                    body.append((char) in.read());
                }
            }
        }

        return new Request(method, url, body.toString(), headers);
    }
}
