package com.automatic.tech_blog.exception;

import com.automatic.tech_blog.dto.response.ErrorResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        log.error(getLimitedStackTrace(ex));
        return switch (ex.getClass().getSimpleName()) {
            case "NullPointerException", "IllegalArgumentException" ->
                returnException(HttpStatus.BAD_REQUEST, ex);
            case "IllegalStateException", "DataIntegrityViolationException" ->
                returnException(HttpStatus.CONFLICT, ex);
            case "FileNotFoundException" ->
                returnException(HttpStatus.NOT_FOUND, ex);
            default ->
                returnException(HttpStatus.INTERNAL_SERVER_ERROR, ex);
        };
    }

    private ResponseEntity<ErrorResponse> returnException(HttpStatus status, Exception exception) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        status.value(),
                        exception.getMessage(),
                        new Date(),
                        false
                ));
    }

    private String getLimitedStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);

        return Arrays.stream(sw.toString().split("\n"))
            .limit(5) // Limit to first 5 lines
            .collect(Collectors.joining("\n"));
    }
}
