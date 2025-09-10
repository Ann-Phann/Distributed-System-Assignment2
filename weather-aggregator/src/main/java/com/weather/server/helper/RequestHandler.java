package com.weather.server.helper;

import com.weather.server.AggregationServer;

/* 
 * Not a thread itself
 * a simple class that contains the logic for processing a single request. 
 * It's an object created and used by the consumer thread from the AggregationServer to do a specific job, and then it's discarded.
 */
public class RequestHandler {
    private RequestNode eachRequestNode;
    private AggregationServer server;

    public RequestHandler(RequestNode node, AggregationServer server) {
        this.eachRequestNode = node;
        this.server = server;
    }
    
    public void run(){}
}
