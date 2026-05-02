package com.donatech.shipping.exception;

import com.donatech.shipping.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<MessageResponse<Void>> handleRouteNotFoundException(RouteNotFoundException ex) {
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ShipmentNotFoundException.class)
    public ResponseEntity<MessageResponse<Void>> handleShipmentNotFoundException(ShipmentNotFoundException ex) {
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<MessageResponse<Void>> handleExternalApiException(ExternalApiException ex) {
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                .message(ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse<Void>> handleGeneralException(Exception ex) {
        MessageResponse<Void> response = MessageResponse.<Void>builder()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Ocurrió un error interno en el servidor: " + ex.getMessage())
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

