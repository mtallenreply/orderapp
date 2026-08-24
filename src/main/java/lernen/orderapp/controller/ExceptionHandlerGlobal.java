package lernen.orderapp.controller;

import lernen.orderapp.service.CustomerNotFoundException;
import lernen.orderapp.service.JobExecutionNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionHandlerGlobal {

    @ExceptionHandler(CustomerNotFoundException.class)
    public Object ProblemDetail(CustomerNotFoundException e){
        log.error("result = {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.getMessage());
    }
    @ExceptionHandler(JobExecutionNotFoundException.class)
    public Object ProblemDetail(JobExecutionNotFoundException e){
        log.error("result = {}", e.getMessage());
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.getMessage());
    }

}
