package archive.oxahex.api.dto;

import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;


@Getter
@RequiredArgsConstructor
public enum SortType {
    ASC("asc"),
    RATING("rating");

    private final String condition;

    public static SortType getSortType(String value) {

        if (value == null || value.isEmpty()) return SortType.ASC;

        return Arrays.stream(SortType.values()).
                filter(x -> x.getCondition().equals(value)).findAny()
                .orElseThrow(() -> new CustomException(ErrorType.INVALID_SORT_TYPE));
    }
}
