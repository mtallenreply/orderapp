package lernen.orderapp.controller;

import jakarta.validation.ConstraintViolationException;
import lernen.orderapp.service.CustomerNotFoundException;
import lernen.orderapp.service.JobExecutionNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public final class ExceptionHandlerGlobal {
    @ExceptionHandler(ConstraintViolationException.class)
    public Object ProblemDetail(final ConstraintViolationException e){
        log.error("result = {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,e.getMessage());
    }
    @ExceptionHandler(CustomerNotFoundException.class)
    public Object ProblemDetail(final CustomerNotFoundException e){
        log.error("result = {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.getMessage());
    }
    @ExceptionHandler(JobExecutionNotFoundException.class)
    public Object ProblemDetail(final JobExecutionNotFoundException e){
        log.error("result = {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object ProblemDetail(final MethodArgumentNotValidException e){
        log.error("result = {}", e.getMessage());
        final Map<String, String> fieldErrors = e.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, fe -> fe.getDefaultMessage() == null ? "ungültig" : fe.getDefaultMessage(), (a, b) -> a));
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validierung fehlgeschlagen");
        problemDetail.setProperty("errors", fieldErrors);
        return problemDetail;
    }

}
