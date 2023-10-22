package archive.oxahex.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class CustomException extends RuntimeException {

    HttpStatus httpStatus;
    String errorMessage;

    public CustomException(ErrorType errorType) {
        this.httpStatus = errorType.getHttpStatus();
        this.errorMessage = errorType.getErrorMessage();
    }
}
