package archive.oxahex.api.controller;

import archive.oxahex.api.dto.ReviewDto;
import archive.oxahex.api.service.AuthService;
import archive.oxahex.api.service.ReviewService;
import archive.oxahex.domain.entity.Review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@PreAuthorize("hasRole('USER') or hasRole('PARTNERS')")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthService authService;

    /**
     * 리뷰 작성
     * 예약 내역에서 이용이 확인된 경우에만 리뷰 작성 가능
     */
    @PostMapping("/reservations/{reservationId}")
    public ResponseEntity<ReviewDto.Info> review(
            @PathVariable Long reservationId,
            @RequestBody @Valid ReviewDto.Request request
    ) {
        Review review = reviewService.addReview(
                reservationId, request.getRating(), request.getContent()
        );

        ReviewDto.Info reviewInfo =
                ReviewDto.fromEntityToReviewInfo(review);

        return ResponseEntity.ok().body(reviewInfo);
    }

    /**
     * 특정 매장 리뷰 조회
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<List<ReviewDto.Info>> getStoreReviews(
            @PathVariable Long storeId
    ) {

        log.info("Review 조회");
        List<Review> reviews = reviewService.getAllReviews(storeId);
        List<ReviewDto.Info> reviewInfos = reviews.stream()
                .map(ReviewDto::fromEntityToReviewInfo).toList();

        return ResponseEntity.ok().body(reviewInfos);
    }
}
