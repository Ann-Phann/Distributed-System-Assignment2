package com.weather.clock;

import java.io.Serializable;

public class LamportClock implements Serializable {
    private int time = 0;

    public LamportClock() {}

    public synchronized int get() {
        return time;
    }

    public synchronized void update(int receivedTime) {
        this.time = Math.max(this.time, receivedTime) + 1;
    }

    public synchronized void increment() {
        this.time++;
    }
    public synchronized int updateAndGet(int time) {
        update(time);
        return this.get();
    }
    
}
