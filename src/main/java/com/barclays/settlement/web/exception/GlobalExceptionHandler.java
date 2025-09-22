package com.barclays.settlement.web.exception;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler({
    IllegalArgumentException.class,
    BindException.class,
    MethodArgumentNotValidException.class
  })
  public ProblemDetail handleBadRequest(Exception exception) {
    log.warn("Bad request", exception);
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    problem.setTitle("Invalid request");
    problem.setDetail(exception.getMessage());
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException exception) {
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    problem.setTitle("Access denied");
    problem.setDetail(exception.getMessage());
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception exception) {
    log.error("Unexpected error", exception);
    ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problem.setTitle("Unexpected error");
    problem.setDetail("An unexpected error occurred. Please contact support.");
    problem.setProperty("timestamp", Instant.now());
    return problem;
  }
}
