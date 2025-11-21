package com.automatic.tech_blog.exception;

import com.automatic.tech_blog.dto.response.ErrorResponse;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler({NullPointerException.class, IllegalArgumentException.class})
  public ResponseEntity<ErrorResponse> handleBadRequestExceptions(RuntimeException ex) {
    log.warn("Bad Request Exception: \n{}", getLimitedStackTrace(ex));
    return returnException(HttpStatus.BAD_REQUEST, ex);
  }

  @ExceptionHandler({IllegalStateException.class, DataIntegrityViolationException.class})
  public ResponseEntity<ErrorResponse> handleConflictExceptions(RuntimeException ex) {
    log.warn("Conflict Exception: \n{}", getLimitedStackTrace(ex));
    return returnException(HttpStatus.CONFLICT, ex);
  }

  @ExceptionHandler(FileNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFoundExceptions(FileNotFoundException ex) {
    log.warn("Not Found Exception: \n{}", getLimitedStackTrace(ex));
    return returnException(HttpStatus.NOT_FOUND, ex);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
    log.error(getLimitedStackTrace(ex));
    return returnException(HttpStatus.INTERNAL_SERVER_ERROR, ex);
  }

  private ResponseEntity<ErrorResponse> returnException(HttpStatus status, Exception exception) {
    return ResponseEntity
        .status(status)
        .body(new ErrorResponse(
            status.value(),
            exception.getLocalizedMessage(),
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
