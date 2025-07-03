package main.service;

import records.ErrorResponse;

public class ErrorHandler {
    private final Exception e;

    public ErrorHandler(Exception e) {
        this.e = e;
    }

    public ErrorResponse handleError(){
        String message = e.getMessage();

        return switch (message) {
            case "Error: Bad request" -> new ErrorResponse(400, message);
            case "Error: Unauthorized" -> new ErrorResponse(401, message);
            case "Error: Already Taken" -> new ErrorResponse(403, message);
            default -> new ErrorResponse(500, message);
        };
    }
}
