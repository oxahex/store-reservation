package archive.oxahex.api.dto;

import archive.oxahex.domain.entity.Reservation;
import archive.oxahex.domain.type.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class ReservationDto {

    @Getter
    @Setter
    public static class Info {
        private Long id;
        private String userName;
        private String storeName;
        private ReservationStatus status;
        private LocalDateTime visitDate;
        private Integer useTableCount;
    }

    public static ReservationDto.Info fromEntityToReservationInfo(Reservation reservation) {
        ReservationDto.Info reservationInfo = new ReservationDto.Info();
        reservationInfo.setId(reservation.getId());
        reservationInfo.setUserName(reservation.getUser().getName());
        reservationInfo.setStoreName(reservation.getStore().getName());
        reservationInfo.setStatus(reservation.getStatus());
        reservationInfo.setVisitDate(reservation.getVisitDate());
        reservationInfo.setUseTableCount(reservation.getUseTableCount());

        return reservationInfo;
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
