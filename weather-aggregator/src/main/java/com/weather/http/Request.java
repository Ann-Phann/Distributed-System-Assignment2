package com.weather.http;

import java.io.Serializable;
import java.util.HashMap;

public class Request implements Serializable {
    private String method;
    private String path;
    private String body;
    private HashMap<String, String> headers;

    public Request(String method, String path, String body, HashMap<String, String> headers) {
        this.method = method;
        this.path = path;
        this.body = body; // JSON format 
        this.headers = headers;
    }
    // getter and setter 
    public HashMap<String, String> getHeaders() { return headers; }

    public void setHeaders(String key, String value) {
        headers.put(key, value);
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethod() { return method; }

    public String getBody() { return body; }

    public void setBody(String body) {
        this.body = body;
    }

    public String getPath() { return path; }

    public void setPath(String path) {
        this.path = path;
    }
}
