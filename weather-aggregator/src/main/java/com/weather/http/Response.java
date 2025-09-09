package com.weather.http;

import java.io.Serializable;
import java.util.HashMap;

public class Response implements Serializable {
    private int statusCode;
    private String body; // JSON
    private HashMap<String, String> headers;

    public HashMap<String, String> getHeaders() { return headers; }

    public void setHeaders(String key, String value) {
        headers.put(key, value);
    }

    public int getStatus() { return statusCode; }

    public void setStatus(int status) {
        this.statusCode = status;
    }

    public String getBody() { return body; }

    public void setBody(String body) {
        this.body = body;
    }

}
