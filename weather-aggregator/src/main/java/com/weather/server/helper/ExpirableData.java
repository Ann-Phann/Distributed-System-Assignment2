package com.weather.server.helper;

public class ExpirableData {
    private String jsonBody;
    private long lastUpdated;

    public ExpirableData(String jsonBody) {
        this.jsonBody = jsonBody;
        this.lastUpdated = System.currentTimeMillis();
    }

    public String getJsonBody() {
        return jsonBody;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void updateTimestamp() {
        this.lastUpdated = System.currentTimeMillis();
    }
}