package archive.oxahex.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorType {

    AUTHENTICATION_FAILURE(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 매장에 대한 권한이 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "유저를 찾을 수 없습니다."),
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 매장입니다."),
    RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 예약 건입니다."),
    PARTNERS_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 파트너스를 찾을 수 없습니다."),
    ALREADY_EXIST_PARTNERS_NAME(HttpStatus.CONFLICT, "이미 존재하는 파트너스 이름입니다."),
    ALREADY_EXIST_USER(HttpStatus.CONFLICT, "이미 존재하는 유저입니다."),
    ALREADY_EXIST_STORE(HttpStatus.CONFLICT, "이미 등록된 매장입니다."),
    ALREADY_EXIST_PARTNERS(HttpStatus.CONFLICT, "이미 등록된 사업자 입니다."),
    BUSINESS_NUMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "등록되지 않은 사업자 번호입니다."),
    UN_MATCH_PARTNERS_USER(HttpStatus.FORBIDDEN, "본인의 사업자 번호가 아닙니다."),
    INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST, "올바른 정렬 형식이 아닙니다."),
    INVALID_SEARCH_CONDITION(HttpStatus.BAD_REQUEST, "올바른 검색 조건이 아닙니다."),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "패스워드가 일치하지 않습니다."),
    TABLE_SOLD_OUT(HttpStatus.BAD_REQUEST, "사용 가능한 테이블이 없습니다."),
    CANCELLABLE_TIME_OUT(HttpStatus.CONFLICT, "예약 시간 8시간 전까지 취소가 가능합니다."),
    TOO_LATE_TO_USE(HttpStatus.CONFLICT, "예약 시간 이후에 도착한 경우 매장 이용이 불가합니다."),
    INVALID_RESERVATION(HttpStatus.CONFLICT, "유효한 예약이 아닙니다."),
    INVALID_REVIEW_REQUEST(HttpStatus.CONFLICT, "매장을 이용한 경우에만 리뷰 작성이 가능합니다."),
    ALREADY_REVIEW_STORE(HttpStatus.CONFLICT, "이미 해당 예약 건에 대해 리뷰를 작성하셨습니다."),
    EXPIRED_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_JWT_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    REDIS_CONNECTION_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "Redis 연결에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String errorMessage;
}
