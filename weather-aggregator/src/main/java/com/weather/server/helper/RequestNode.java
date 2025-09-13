package com.weather.server.helper;

import com.weather.http.Request;

import java.net.Socket;

public class RequestNode {
    private final Socket socket;
    private final Request request;
    private final int lamportClockValue;

    public RequestNode(Socket socket, Request request, int lamportClockValue) {
        this.socket = socket;
        this.request = request;
        // Get the Lamport clock value from the request headers
        // this.lamportClockValue = Integer.parseInt(request.getHeaders().get("Lamport-Clock"));
        this.lamportClockValue = lamportClockValue;
    }

    public Socket getSocket() { return socket; }
    public Request getRequest() { return request; }
    public int getLamportClockValue() { return lamportClockValue; }
}
