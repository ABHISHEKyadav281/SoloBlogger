package com.solo.blogger.dto;

import com.solo.blogger.exception.TokenExpiredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.jsonwebtoken.ExpiredJwtException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        return new ResponseEntity<>(ApiResponseDto.error("400", ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleNullPointerException(
            NullPointerException ex, WebRequest request) {
        return new ResponseEntity<>(ApiResponseDto.error("500", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<Void>> handleAllExceptions(
            Exception ex, WebRequest request) {
        return new ResponseEntity<>(ApiResponseDto.error("500", ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ApiResponseDto<Void>> handleTokenExpiredException(
            TokenExpiredException ex, WebRequest request) {
        return new ResponseEntity<>(ApiResponseDto.error("401", ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

}
