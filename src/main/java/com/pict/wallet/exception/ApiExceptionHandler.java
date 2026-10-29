package com.pict.wallet.exception;

import com.pict.wallet.controller.WalletApiController;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestControllerAdvice(assignableTypes = WalletApiController.class)
public class ApiExceptionHandler {
    @ExceptionHandler(WalletException.class)
    public ResponseEntity<Map<String, String>> business(WalletException exception) {
        return ResponseEntity.status(exception.getStatus()).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        return ResponseEntity.badRequest().body(Map.of("message", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> malformed() {
        return ResponseEntity.badRequest().body(Map.of("message", "Invalid JSON or amount"));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> duplicate() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Account already exists or data conflicts"));
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<Map<String, String>> busy() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "Wallet is busy. Check history, then try again"));
    }
}
