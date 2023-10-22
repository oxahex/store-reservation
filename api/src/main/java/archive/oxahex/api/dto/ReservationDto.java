package archive.oxahex.api.dto;

import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.type.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class ReservationDto {

    /**
     * 매장 예약 요청
     */
    @Getter
    @Setter
    public static class Request {

        @NotBlank(message = "방문 일자를 입력해주세요")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        private LocalDateTime visitedDate;

        @NotBlank(message = "사용할 테이블 수를 입력해주세요.")
        @Min(value = 0, message = "최소 한 자리 이상 입력해주세요.")
        private Integer useTableCount;
    }

    /**
     * 예약 내역 상세
     */
    @Getter
    @Setter
    public static class Detail {

        private Long id;
        private UserDto.Info user;
        private StoreDto.Info store;
        private LocalDateTime visitDate;
        private ReservationStatus status;
        private Integer useTableCount;
    }


    public static ReservationDto.Detail fromEntityToReservationDetail(Reservation reservation) {

        ReservationDto.Detail reservationDetail = new ReservationDto.Detail();
        reservationDetail.setId(reservation.getId());
        reservationDetail.setUser(UserDto.fromEntityToUserInfo(reservation.getUser()));
        reservationDetail.setStore(StoreDto.fromEntityToStoreInfo(reservation.getStore()));
        reservationDetail.setVisitDate(reservation.getVisitDate());
        reservationDetail.setStatus(reservation.getStatus());
        reservationDetail.setUseTableCount(reservation.getUseTableCount());

        return reservationDetail;
    }
}
