package archive.oxahex.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    ALREADY_EXIST_USER(HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "패스워드가 일치하지 않습니다."),
    ACCESS_DENIED(HttpStatus.UNAUTHORIZED, "권한이 없습니다."),
    ALREADY_EXIST_PARTNERS(HttpStatus.CONFLICT, "이미 등록된 사업자 입니다."),
    BUSINESS_NUMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "등록되지 않은 사업자 번호입니다."),
    ALREADY_EXIST_STORE(HttpStatus.CONFLICT, "이미 등록된 매장입니다."),
    UN_MATCH_PARTNERS_USER(HttpStatus.FORBIDDEN, "본인의 사업자 번호가 아닙니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;
}
