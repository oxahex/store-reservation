package archive.oxahex.api.handler;

import archive.oxahex.api.dto.response.ErrorResponse;
import archive.oxahex.api.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> bindingException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        log.error("[MethodArgumentNotValidException]", e);

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                bindingResult.getAllErrors().get(0).getDefaultMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> customException(CustomException e) {

        log.error("[CustomException]", e);
        ErrorResponse errorResponse = new ErrorResponse(
                e.getHttpStatus().value(),
                e.getErrorMessage()
        );

        return new ResponseEntity<>(errorResponse, e.getHttpStatus());
    }

    // TODO: 컨트롤러 말고 그 이전 단계에서 잡히는 에러 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> accessDeniedException(AccessDeniedException e) {
        log.error("[AccessDeniedException]", e);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * 정의 되지 않은 예외 처리
     */
    @ExceptionHandler
    public ResponseEntity<ErrorResponse> serverError(Exception e) {

        log.error("[Internal Server Error]", e);
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
