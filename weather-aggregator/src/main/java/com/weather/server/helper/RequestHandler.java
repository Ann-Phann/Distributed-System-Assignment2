package com.weather.server.helper;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.weather.http.Request;
import com.weather.http.Response;
import com.weather.http.StatusCode;
import com.weather.server.AggregationServer;

/* 
 * Not a thread itself
 * Handle both GET and PUT request
 * a simple class that contains the logic for processing a single request. 
 * It's an object created and used by the consumer thread from the AggregationServer to do a specific job, and then it's discarded.
 */
public class RequestHandler {
    private RequestNode eachRequestNode;
    private AggregationServer server;
    private Storage storage;

    public RequestHandler(RequestNode node, AggregationServer server, Storage storage) {
        this.eachRequestNode = node;
        this.server = server;
        this.storage = storage;
    }
    
    public void run(){
        Socket clientSocket = eachRequestNode.getSocket();
        Request request = eachRequestNode.getRequest();

        try {
            switch (request.getMethod()) {
                case "GET":
                    handleGetRequest(request, clientSocket);
                    break;
                
                case "PUT":
                    handlePutRequest(request, clientSocket);
                    break;

                default:
                    // Send 400 Bad Request for unsupported methods
                    Response response = new Response(StatusCode.BAD_REQUEST);
                    ResponseSender.sendResponse(clientSocket, response);
                    break;
            }
        } catch (IOException e) {
            System.err.println("Error handling request: " + e.getMessage());
        } finally {
            try {
                // Ensure the socket is always closed after handling the request
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }

    }

    public void handlePutRequest(Request request, Socket clientSocket) throws IOException {
        ConcurrentHashMap<String, String> weatherData = server.getWeatherData();
        Semaphore fileLock = server.getFileLock();
        
        // get the Lamport Clock from RequestNode to serve as uniqueId for Storage
        String uniqueId = String.valueOf(eachRequestNode.getLamportClockValue());
        try {
            fileLock.acquire();
            // the path format is /weather/<StationID>
            String id = request.getPath().substring(request.getPath().lastIndexOf('/') + 1);
            weatherData.put(id, request.getBody());
            System.out.println("PUT request for " + id + " handled. Data stored.");

            // Send a 200 OK response
            Response response = new Response(StatusCode.OK);
            response.setBody("Success: Data saved for " + id);
            response.addHeaders("Content-Type", "text/plain");
            response.addHeaders("Content-Length", String.valueOf(response.getBody().length()));

            // add Lamport Clock into response headers
            response.addHeaders("Lamport-Clock", String.valueOf(server.getClock().get()));

            ResponseSender.sendResponse(clientSocket, response);

            // mark in Storage as completed (COMMIT)
            storage.logCompletion(uniqueId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            fileLock.release();
        }
    }

    private void handleGetRequest(Request request, Socket socket) throws IOException {
        ConcurrentHashMap<String, String> weatherData = server.getWeatherData();
        Semaphore fileLock = server.getFileLock();

        try {
            fileLock.acquire();
            String path = request.getPath();
            Response response;
            
            if (path.matches("/weather/.+")) {
                // format path: "/weather/<stationId>"
                String id = path.substring(path.lastIndexOf('/') + 1);
                String data = weatherData.get(id);

                if (data != null) {
                    // found the data for a specific station
                    response = new Response(StatusCode.OK); 
                    response.setBody(data);
                    response.addHeaders("Content-Type", "application/json");
                    response.addHeaders("Content-Length", String.valueOf(data.length()));
                } else {
                    response = new Response(StatusCode.NOT_FOUND);
                }
            } else if (path.equals("/weather/")) {
                // this path is for all stations
                // it return all the key values from weatherData map
                // Before: JsON object ["ID001", "ID002"] --> After ObjectMapper "[\"ID001\",\"ID002\"]"
                String allStationId = new ObjectMapper().writeValueAsString(weatherData.keySet()); 
                response = new Response(StatusCode.OK);
                response.setBody(allStationId);
                response.addHeaders("Content-Type", "application/json");
                response.addHeaders("Content-Length", String.valueOf(allStationId.length()));
            } else {
                // invalid path 
                response = new Response(StatusCode.BAD_REQUEST);
            }

            // add the server lamport clock to response headers
            response.addHeaders("Lamport-Clock", String.valueOf(server.getClock().get()));

            ResponseSender.sendResponse(socket, response);
            
        } catch (InterruptedException | JsonProcessingException e) {
            Thread.currentThread().interrupt();
            ResponseSender.sendResponse(socket, new Response(StatusCode.INTERNAL_SERVER_ERROR));
        } finally {
            fileLock.release();
        }
    }
}
