package lernen.orderapp.controller;



import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@RestControllerAdvice
public class ExceptionHandlerGlobal {
    private final DateTimeFormatter df=DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(RuntimeException.class)
    public DTO.FehlerResponse handleRuntimeException(Exception e) {
        System.out.println("handleRuntimeException");
        return new DTO.FehlerResponse(
                LocalDateTime.now().format(df),
                500,
                e.getMessage()
        );
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public DTO.FehlerResponse handleKontoNichtGefundenException(Exception e) {
        System.out.println("handleKontoNichtGefundenException");
        return new DTO.FehlerResponse(
                LocalDateTime.now().format(df),
                404,
                e.getMessage()
        );
    }
}
