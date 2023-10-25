package archive.oxahex.api.dto;

import archive.oxahex.domain.entity.Review;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDateTime;


public class ReviewDto {

    @Getter
    @Setter
    public static class Request {

        @NotNull(message = "별점을 입력해주세요.")
        @Range(min = 0, max = 5)
        public Integer rating;

        @NotBlank(message = "리뷰 내용을 입력해주세요.")
        @Length(min = 10, max = 500, message = "리뷰는 최소 10자 이상, 500자 미만으로 입력할 수 있습니다.")
        public String content;
    }

    @Getter @Setter
    public static class Response {}

    @Getter
    @Setter
    public static class Info {
        private Long id;
        private String userName;
        private String storeName;
        private Integer rating;
        private String content;
        private LocalDateTime createdDate;
    }

    public static ReviewDto.Info fromEntityToReviewInfo(Review review) {
        ReviewDto.Info reviewInfo = new ReviewDto.Info();
        reviewInfo.setId(review.getId());
        reviewInfo.setUserName(review.getUser().getName());
        reviewInfo.setUserName(review.getStore().getName());
        reviewInfo.setRating(review.getRating());
        reviewInfo.setContent(review.getContent());
        reviewInfo.setCreatedDate(review.getCreatedDate());

        return reviewInfo;
    }

}
