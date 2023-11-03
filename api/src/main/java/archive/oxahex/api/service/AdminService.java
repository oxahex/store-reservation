package archive.oxahex.api.service;

import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.domain.entity.Review;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminService {

    private final ReviewRepository reviewRepository;

    @Transactional
    public Review deleteReview(Long reviewId) {

        // 리뷰
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ErrorType.REVIEW_NOT_FOUND));

        review.getStore().decreaseReviewCount();
        reviewRepository.delete(review);

        return review;
    }
}
