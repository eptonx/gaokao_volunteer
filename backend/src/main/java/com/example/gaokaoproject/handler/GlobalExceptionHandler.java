package com.example.gaokaoproject.handler;

import com.example.gaokaoproject.exception.ServiceException;
import com.example.gaokaoproject.res.Result;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    @Order(1)
    public Result<?> serviceException(ServiceException e) {
        return new Result<>(e.getCode(), e.getMsg());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @Order(2)
    public Result<?> validationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return new Result<>(400, msg);
    }

    @ExceptionHandler(Exception.class)
    @Order(0)
    public Result<?> exception(Exception e) {
        log.error(e.getMessage(), e);
        return Result.error(e.getMessage());
    }
}
