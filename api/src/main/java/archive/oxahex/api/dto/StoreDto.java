package archive.oxahex.api.dto;

import archive.oxahex.domain.entity.Store;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class StoreDto {

    @Getter
    @Setter
    public static class Info {
        private Long id;
        private String name;
        private String address;
        private String description;
        private Integer tableCount;
    }

    public static StoreDto.Info fromEntityToStoreInfo(Store store) {
        StoreDto.Info storeInfo = new StoreDto.Info();
        storeInfo.setId(store.getId());
        storeInfo.setName(store.getName());
        storeInfo.setAddress(store.getAddress());
        storeInfo.setDescription(store.getDescription());
        storeInfo.setTableCount(store.getTableCount());

        return storeInfo;
    }

    @Getter
    @Setter
    public static class Detail {

        private Long id;
        private String name;
        private String address;
        private String description;
        private Integer tableCount;
        private Integer reviewCount;
    }

    public static StoreDto.Detail fromEntityToStoreDetail(Store store) {
        StoreDto.Detail storeDetail = new StoreDto.Detail();
        storeDetail.setId(store.getId());
        storeDetail.setName(store.getName());
        storeDetail.setAddress(store.getAddress());
        storeDetail.setDescription(store.getDescription());
        storeDetail.setTableCount(store.getTableCount());
        storeDetail.setReviewCount(store.getReviewCount());

        return storeDetail;
    }

    @Getter
    @Setter
    public static class Request {

        @NotBlank(message = "매장 이름을 입력해주세요.")
        private String name;

        @NotBlank(message = "매장 주소를 입력해주세요.")
        private String address;

        @NotBlank(message = "매장 설명을 입력해주세요.")
        private String description;

        @NotNull(message = "사용 가능한 테이블 수를 입력해주세요.")
        private Integer tableCount;

        @NotBlank(message = "해당 매장의 사업자 등록 번호를 입력해주세요")
        private String businessNumber;
    }

    public static class Response {
        private UserDto.Info user;
        private StoreDto.Info store;
    }


}
