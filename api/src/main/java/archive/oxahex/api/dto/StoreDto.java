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
}
