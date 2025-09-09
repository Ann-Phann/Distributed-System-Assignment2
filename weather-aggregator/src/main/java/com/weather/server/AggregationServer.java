package com.weather.server;

import com.weather.clock.LamportClock;
import com.weather.http.Request;
import com.weather.server.helper.*;

import java.net.ServerSocket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Semaphore;

/*
 * Primary job is to act as listener and dispatcher. It sits in a loop, waiting for new client connections to arrive.
 * Delegate request to new thread for processing.
 */
public class AggregationServer {
    private int port; // default = 4567 
    private PriorityBlockingQueue<RequestNode> requestQueue; // store both PUT and GET request
    private LamportClock clock;
    private ConcurrentHashMap<String, String> weatherData; 
    private ServerSocket serverSocket;
    private final Semaphore fileLock;

    private Storage storage; // for persistence storage

    public AggregationServer(int port) {
        this.port = port;
        this.requestQueue = new PriorityBlockingQueue<RequestNode>();
        this.clock = new LamportClock();
        this.weatherData = new ConcurrentHashMap<>();
        this.fileLock = new Semaphore(1, true); // ensure only 1 thread writing to the Storage
        
    }

    public void run() {
        System.out.println("Starting Aggregation Server on port " + this.port + "...");

    }
    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("ERROR: Invalid numbers of arguments. Usage: <port number>");
            return;
        }

        int port = 4567; // default
        try {
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            } 

            AggregationServer server = new AggregationServer(port);
        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid port number format. Please enter a valid integer.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during server startup: " + e.getMessage());
            e.printStackTrace();
        }
        
    }

}
