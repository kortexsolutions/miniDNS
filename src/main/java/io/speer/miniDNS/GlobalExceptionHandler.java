package io.speer.miniDNS;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex, HttpServletRequest request) {
        String errorMessage = ex.getReason() != null ? ex.getReason() : "An unexpected error has occurred.";

        return ResponseEntity
            .status(ex.getStatusCode())
            .body(Map.of(
                "message", errorMessage,
                "error", ((HttpStatus) ex.getStatusCode()).getReasonPhrase(),
                "code", String.valueOf(ex.getStatusCode().value()),
                "timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "path", request.getRequestURI()
            ));
    }
}








