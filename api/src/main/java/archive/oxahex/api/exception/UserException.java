package archive.oxahex.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class UserException extends RuntimeException {

    HttpStatus httpStatus;
    String errorMessage;

    public UserException(ErrorType errorType) {
        this.httpStatus = errorType.getHttpStatus();
        this.errorMessage = errorType.getErrorMessage();
    }
}
