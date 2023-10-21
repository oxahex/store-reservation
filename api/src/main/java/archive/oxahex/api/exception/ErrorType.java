package archive.oxahex.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    USER_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;
}
