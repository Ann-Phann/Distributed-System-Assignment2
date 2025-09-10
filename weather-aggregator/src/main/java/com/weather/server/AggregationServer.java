package com.weather.server;

import com.weather.clock.LamportClock;
import com.weather.http.Request;
import com.weather.server.helper.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

/*
 * Primary job is to act as listener and dispatcher. It sits in a loop, waiting for new client connections to arrive.
 * Delegate request to new thread for processing.
 * 
 * AggregationServer class implements Runnable to allow the server's main loop to run in its own, dedicated thread.
 */
public class AggregationServer implements Runnable {
    private volatile boolean isRunning = true;
    private int port; // default = 4567 
    private PriorityBlockingQueue<RequestNode> requestQueue; // store both PUT and GET request
    private LamportClock clock;
    private ConcurrentHashMap<String, String> weatherData; 
    private ServerSocket serverSocket;
    private final Semaphore fileLock;

    
    private Storage storage; // for persistence storage

    public AggregationServer(int port) {
        this.port = port;
        this.requestQueue = new PriorityBlockingQueue<RequestNode>(11, Comparator.comparingLong(request -> request.getLamportClockValue()));
        this.clock = new LamportClock();
        this.weatherData = new ConcurrentHashMap<>();
        this.fileLock = new Semaphore(1, true); // ensure only 1 thread writing to the Storage
    }

    @Override
    public void run() {
        System.out.println("Starting Aggregation Server on port " + this.port + "...");

        try {
            this.serverSocket = new ServerSocket(this.port);

            // Start Producer thread (Listener)
            RequestListener listener = new RequestListener(this, serverSocket, requestQueue);
            new Thread(listener).start();

            // start the Consumer thread.
            Thread consumerThread = new Thread(() -> {
            try {
                // The main loop for the consumer thread
                while (this.isRunning) {
                    // Take a RequestNode from the queue when available. This call blocks until an item is available.
                    RequestNode requestNode = requestQueue.take();
                    // Pass the RequestNode to a handler to process it.
                    // This creates a temporary object to handle the request logic.
                    RequestHandler handler = new RequestHandler(requestNode, this);
                    handler.run();
                }
            } catch (InterruptedException e) {
                // Handle thread interruption
                Thread.currentThread().interrupt();
            }
        });
        consumerThread.start();

        } catch (IOException e) {
            System.err.println("Could not listen on port " + this.port);
            System.exit(-1);
        } catch (Exception e) {
            System.err.println("ERROR: when running Aggregation Server: "+ e.getMessage());
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            isRunning = false;
            
        } catch (IOException  e) {
            System.err.println("ERROR: An error occurred while closing the server socket.");
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("ERROR: Invalid numbers of arguments. Usage: <port number>");
            return;
        }

        int port = 4567; // default
        try {
            if (args.length > 0) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.err.println("ERROR: Invalid port number format. Please enter a valid integer.");
                    return; // Exit if the format is wrong
                }
            } 

            Thread server = new Thread(new AggregationServer(port));
            server.start();
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid port number format. Please enter a valid integer.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during server startup: " + e.getMessage());
            e.printStackTrace();
        }
        
    }

}
