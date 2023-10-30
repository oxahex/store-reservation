package archive.oxahex.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReservationRequest {

    @NotNull(message = "방문 일자를 입력해주세요")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    @Future(message = "과거 시간은 입력할 수 없습니다.")
    private LocalDateTime visitedDate;

    @Min(value = 1, message = "사용할 테이블 수를 최소 한 자리 이상 입력해주세요.")
    private int useTableCount;
}
