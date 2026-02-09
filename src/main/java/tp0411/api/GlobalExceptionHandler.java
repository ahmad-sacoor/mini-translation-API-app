package tp0411.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import tp0411.tickets.TicketAlreadyTranslatedException;
import tp0411.tickets.TicketNotFoundException;
import tp0411.tickets.TicketNotTranslatedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .orElse("Invalid request");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("BAD_REQUEST", "Malformed JSON request body"));
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TicketNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(TicketAlreadyTranslatedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyTranslated(TicketAlreadyTranslatedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", ex.getMessage()));
    }

    @ExceptionHandler(TicketNotTranslatedException.class)
    public ResponseEntity<ErrorResponse> handleNotTranslated(TicketNotTranslatedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", ex.getMessage()));
    }

    // Day 4: maps ResponseStatusException (like 502) to our consistent JSON error shape
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        String code =
                status == HttpStatus.BAD_GATEWAY ? "BAD_GATEWAY" :
                        status == HttpStatus.CONFLICT ? "CONFLICT" :
                                status == HttpStatus.NOT_FOUND ? "NOT_FOUND" :
                                        status == HttpStatus.BAD_REQUEST ? "BAD_REQUEST" :
                                                status.name();

        String message = ex.getReason() == null ? "Request failed" : ex.getReason();

        return ResponseEntity.status(status)
                .body(new ErrorResponse(code, message));
    }
}
