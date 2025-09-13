package com.weather.server.handler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import com.weather.server.helper.ExpirableData;

public class DataExpirer implements Runnable {
    private static final long EXPIRATION_THRESHOLD_SECOND = 30 * 1000; // 30 seconds
    private ConcurrentHashMap<String, ExpirableData> weatherData;
    private Semaphore fileLock;

    public DataExpirer(ConcurrentHashMap<String, ExpirableData> weatherData, Semaphore fileLock) {
        this.weatherData = weatherData;
        this.fileLock = fileLock;
    }

    @Override
    public void run() {
        System.out.println("Running data expiration task...");
        long currentTime = System.currentTimeMillis();
        try {
            // Acquire the lock to ensure no other threads are writing to the map during expiration
            fileLock.acquire();
            
            weatherData.entrySet().removeIf(entry -> {
                long lastUpdated = entry.getValue().getLastUpdated();
                if (currentTime - lastUpdated > EXPIRATION_THRESHOLD_SECOND) {
                    System.out.println("Expiring data for station: " + entry.getKey());
                    return true;
                }
                return false;
            });
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Data expiration task interrupted.");
        } finally {
            fileLock.release();
        }
    }
}