package archive.oxahex.api.handler;

import archive.oxahex.api.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> bindingException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        log.error("[MethodArgumentNotValidException]", e);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                bindingResult.getAllErrors().get(0).getDefaultMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 정의 되지 않은 예외 처리
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> serverError(Exception e) {

        log.error("[Internal Server Error]", e);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
