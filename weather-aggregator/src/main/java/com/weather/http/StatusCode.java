package com.weather.http;

public enum StatusCode {
    OK(200),
    BAD_REQUEST(400),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR((500));

    private final int code;
    
    StatusCode(int code) {
        this.code = code;
    }

    public int getStatusCode() {
        return code;
    }

    public static StatusCode fromCode(int code) {
        for (StatusCode status : StatusCode.values()) {
            if (status.getStatusCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}
