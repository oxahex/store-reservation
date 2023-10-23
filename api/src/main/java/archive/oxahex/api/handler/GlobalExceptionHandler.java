package archive.oxahex.api.handler;

import archive.oxahex.api.dto.ErrorDto;
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
    public ResponseEntity<ErrorDto> bindingException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();

        log.error("[MethodArgumentNotValidException]", e);

        ErrorDto errorResponse = new ErrorDto(
                HttpStatus.BAD_REQUEST.value(),
                bindingResult.getAllErrors().get(0).getDefaultMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorDto> customException(CustomException e) {

        log.error("[CustomException]", e);
        ErrorDto errorResponse = new ErrorDto(
                e.getHttpStatus().value(),
                e.getErrorMessage()
        );

        return new ResponseEntity<>(errorResponse, e.getHttpStatus());
    }

    // TODO: 컨트롤러 말고 그 이전 단계에서 잡히는 에러 처리
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> accessDeniedException(AccessDeniedException e) {
        log.error("[AccessDeniedException]", e);
        ErrorDto errorResponse = new ErrorDto(
                HttpStatus.FORBIDDEN.value(),
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    /**
     * 정의 되지 않은 예외 처리
     */
    @ExceptionHandler
    public ResponseEntity<ErrorDto> serverError(Exception e) {

        log.error("[Internal Server Error]", e);
        ErrorDto errorResponse = new ErrorDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getMessage()
        );

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

    }

}
