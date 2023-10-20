package archive.oxahex.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;


public class ReviewDto {


    @Getter
    @Setter
    public static class Request {

        @NotNull(message = "유저를 확인할 수 없습니다.")
        public Long userId;
        @NotNull(message = "매장을 확인할 수 없습니다.")
        public Long storeId;
        @NotNull(message = "별점을 입력해주세요.")
        @Range(min = 0, max = 5)
        public Integer rating;

        @NotBlank(message = "리뷰 내용을 입력해주세요.")
        @Range(min = 10, max = 500, message = "리뷰는 최소 10자 이상, 500자 미만으로 입력할 수 있습니다.")
        public String content;
    }

    @Getter @Setter
    public static class Response {}
}
