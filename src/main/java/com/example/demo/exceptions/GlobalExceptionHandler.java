package com.example.demo.exceptions;

import com.example.demo.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiResponse<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.error(ex.getMessage());
        return new ApiResponse<>("Failed", ex.getMessage(), null);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiResponse<String> handleValidationException(ValidationException ex) {
        logger.error(ex.getMessage());
        return new ApiResponse<>("Failed", "Error: " + ex.getMessage(), null);
    }

    @ExceptionHandler(MailSendingException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ResponseBody
    public ApiResponse<String> handleMailSendingException(MailSendingException ex) {
        logger.error(ex.getMessage());
        return new ApiResponse<>("Failed", "Error: " + ex.getMessage(), null);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ApiResponse<String> handleUserNotFoundException(UserNotFoundException ex) {
        logger.error(ex.getMessage());
        return new ApiResponse<>("Failed", "Error: " + ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    public ApiResponse<String> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        logger.error(ex.getMessage());
        return new ApiResponse<>("Failed", "Error: " + ex.getMessage(), null);

    }

    @ExceptionHandler(AccountAlreadyActivatedException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    public ApiResponse<String> handleAccountAlreadyActivatedException(AccountAlreadyActivatedException ex) {
        logger.error(ex.getMessage());
        return new ApiResponse<>("Failed", "Error: " + ex.getMessage(), null);

    }

    @ExceptionHandler(RiderUnavailableException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    public ApiResponse<String> handleRiderUnavailableException(RiderUnavailableException ex) {
        logger.error(ex.getMessage());
        return new ApiResponse<>("Failed", "Error: " + ex.getMessage(), null);

    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    public ApiResponse<String> handleResourceAlreadyExistsException(ResourceAlreadyExistsException ex) {
        logger.error(ex.getMessage());
        return new ApiResponse<>("Failed", "Error: " + ex.getMessage(), null);

    }

    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    public ApiResponse<String> handleUnsupportedOperationException(UnsupportedOperationException ex) {
        logger.error(ex.getMessage());
        return new ApiResponse<>("Failed", "Error: " + ex.getMessage(), null);

    }

    @ExceptionHandler(InputMismatchException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    @ResponseBody
    public ApiResponse<String> handleInputMismatchException(InputMismatchException ex) {
        logger.error(ex.getMessage());
        return new ApiResponse<>("Failed", "Error: " + ex.getMessage(), null);

    }
}

