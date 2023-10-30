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
        reviewInfo.setStoreName(review.getStore().getName());
        reviewInfo.setRating(review.getRating());
        reviewInfo.setContent(review.getContent());
        reviewInfo.setCreatedDate(review.getCreatedDate());

        return reviewInfo;
    }

}
