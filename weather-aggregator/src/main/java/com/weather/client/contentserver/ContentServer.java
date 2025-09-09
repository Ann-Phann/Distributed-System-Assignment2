package com.weather.client.contentserver;

import com.weather.client.AbstractClient;
import com.weather.http.Request;
import com.weather.http.Response;

public class ContentServer extends AbstractClient {
    public String id;
    String dir = "src/main/java/client/contentserver/data";

    public ContentServer(String stationId) {
        this.id = stationId;
    }

    @Override
    protected Request createRequest() {

    }

    @Override
    protected void showResponse(Response response) {

    }
}
