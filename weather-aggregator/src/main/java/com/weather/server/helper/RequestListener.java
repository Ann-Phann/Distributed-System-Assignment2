package com.weather.server.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.PriorityBlockingQueue;

import com.weather.http.Request;
import com.weather.server.AggregationServer;

public class RequestListener implements Runnable {
    private final AggregationServer server;
    private final ServerSocket serverSocket;
    private final PriorityBlockingQueue<RequestNode> requestQueue;

    public RequestListener(AggregationServer server, ServerSocket serverSocket, PriorityBlockingQueue<RequestNode> requestQueue) {
        this.server = server;
        this.serverSocket = serverSocket;
        this.requestQueue = requestQueue;
    }
    @Override
    public void run() {
        System.out.println("RequestListener is running and listening for client connections.");
        try {
            // Set a timeout to prevent the thread from blocking indefinitely on serverSocket.accept()
            this.serverSocket.setSoTimeout(1000); 
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Accept a new client connection
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected from: " + clientSocket.getInetAddress());

                    // Read the request from the client's input stream
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    Request request = RequestParser.parse(in);

                    // Create a RequestNode and add it to the shared queue
                    RequestNode requestNode = new RequestNode(clientSocket, request);
                    requestQueue.put(requestNode); // this will block adding in if the queue is full
                    System.out.println("Request from " + clientSocket.getInetAddress() + " added to queue. Queue size: " + requestQueue.size());

                } catch (SocketTimeoutException e) {
                    // This is expected and allows the loop to check the thread's interruption status
                } catch (IOException e) {
                    System.err.println("Error accepting or processing client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Listener thread failed to initialize or experienced a fatal error: " + e.getMessage());
        } finally {
            // cleanup the whole server
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing server socket: " + e.getMessage());
            }
        }
        System.out.println("RequestListener thread shutting down.");
    }
}
