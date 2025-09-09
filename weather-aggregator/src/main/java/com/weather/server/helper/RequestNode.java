package com.weather.server.helper;

import com.weather.http.Request;

import java.net.Socket;

public class RequestNode {
    private final Socket socket;
    private final Request request;
    private final long lamportClockValue;

    public RequestNode(Socket socket, Request request) {
        this.socket = socket;
        this.request = request;
        // Get the Lamport clock value from the request headers
        this.lamportClockValue = Long.parseLong(request.getHeaders().get("Lamport-Clock"));
    }

    public Socket getSocket() { return socket; }
    public Request getRequest() { return request; }
    public long getLamportClockValue() { return lamportClockValue; }
}
