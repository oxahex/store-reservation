package archive.oxahex.domain.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum ReservationStatus {
    PENDING("pending"),
    CANCELLED("cancelled"),
    ALLOWED("allowed"),
    REJECTED("rejected"),
    CONFIRMED("confirmed");

    private final String status;

    public static Optional<ReservationStatus> valueToEnum(String value) {
        if (value == null) return Optional.empty();

        return Arrays.stream(ReservationStatus.values())
                .filter(v -> v.getStatus().equals(value))
                .findAny();
    }
}
