package io.hhplus.tdd.exception;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@RequiredArgsConstructor
class ApiControllerAdvice extends ResponseEntityExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(ApiControllerAdvice.class);

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(500).body(new ErrorResponse("500", "에러가 발생했습니다."));
    }

    @ExceptionHandler(CustomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleCompAttendCustomException(CustomException ex) {
        String code = ex.getCode();
        String message = ex.getMessage();
        logger.error("ApiControllerAdvice : {} - {} : {}", code, message, ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(code,message));
    }
}
