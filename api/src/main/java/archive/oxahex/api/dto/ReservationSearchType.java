package archive.oxahex.api.dto;

import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

@Getter
@RequiredArgsConstructor
public enum ReservationSearchType {

    ALL("all"),
    PENDING("pending"),
    ALLOWED("allowed"),
    REJECTED("rejected"),
    CONFIRMED("confirmed");

    private final String condition;

    public static ReservationSearchType getReservationSearchType(String value) {
        if (value == null || value.isEmpty()) return ReservationSearchType.ALL;

        return Arrays.stream(ReservationSearchType.values())
                .filter(x -> Objects.equals(x.getCondition(), value)).findAny()
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_SEARCH_CONDITION));
    }
}
