package com.weather.client.getclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.client.AbstractClient;
import com.weather.http.Request;
import com.weather.http.Response;

public class GetClient extends AbstractClient {
    public String stationId;

    // constructor 
    public GetClient(String host_name, int port, String stationId) {
        super(host_name, port);
        this.stationId = stationId;
    }

    @Override
    protected Request createRequest() {
        String body = "";

        // Station ID is optional 
        String path;
        if (this.stationId != null && !this.stationId.isEmpty()) {
            path = "/weather/" + this.stationId;
        } else {
            path = "/weather/";
        }

        HashMap<String,String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        Request request = new Request("GET", path, body, headers);
        return request;
    }

    @Override
    protected void showResponse(Response response) {
        int status = response.getStatus();
        String body = response.getBody();
        HashMap<String, String> headers = response.getHeaders();

        System.out.println("--- Response --- ");
        System.out.println("Status: " + status);

        System.out.println("Headers:");
        for (String key : headers.keySet()) {
            System.out.println("\t" + key + ": " + headers.get(key));
        }

        System.out.println("----------------");

        // check for success status
        if (status == 200) {
            System.out.println("Body:");
            try {
                // JSON Parsing: deserialisation
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode node = objectMapper.readTree(body);

                // Iterate over all fields in the JSON object
                Iterator<Entry<String, JsonNode>> fields = node.fields();
                while (fields.hasNext()) {
                    Entry<String, JsonNode> entry = fields.next();
                    System.out.println("  " + entry.getKey() + ": " + entry.getValue().asText());
                }

            } catch (IOException e) {
                System.err.println("Error parsing JSON response: " + e.getMessage());
            }
            
        } else {
            System.err.println("Error: " + body);
        }
    }


    /*
     * param args: server URL in the form of hostname:port
     */

    public static void main(String[] args) {
        // check edge case 
        if (args.length < 1 || args. length > 2) {
            System.out.println("Please send correct format: '<hostname>:<port> <(Optional)Station ID>'");
        }

        try {
            
            String[] path = args[0].split(":", 2);
            String hostname = path[0];
            int port = Integer.parseInt(path[1]);
            
            String stationId;
            if (args.length == 2) {
                stationId = args[1];
            } else {
                stationId = null;
            }

            GetClient client = new GetClient(hostname, port, stationId);
            client.requestAndResponse();
            
        } catch (Exception e) {
            System.err.println("Error parsing arguments or connecting to server.");
            e.printStackTrace();
        }
    }
}
