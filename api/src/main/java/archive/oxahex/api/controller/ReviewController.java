package archive.oxahex.api.controller;

import archive.oxahex.api.dto.ReviewDto;
import archive.oxahex.api.dto.request.ReviewModifyRequest;
import archive.oxahex.api.dto.request.ReviewRequest;
import archive.oxahex.api.security.AuthUser;
import archive.oxahex.api.service.AuthService;
import archive.oxahex.api.service.ReviewService;
import archive.oxahex.domain.entity.Review;

import archive.oxahex.domain.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthService authService;

    /**
     * 리뷰 작성
     * 예약 내역에서 이용이 확인된 경우에만 리뷰 작성 가능
     */
    @PostMapping("/reservations/{reservationId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewDto.Info> review(
            @PathVariable Long reservationId,
            @RequestBody @Valid ReviewRequest request
    ) {
        Review review = reviewService.addReview(
                reservationId, request.getRating(), request.getContent()
        );

        ReviewDto.Info reviewInfo =
                ReviewDto.fromEntityToReviewInfo(review);

        return ResponseEntity.ok().body(reviewInfo);
    }

    /**
     * 리뷰 수정
     * 해당 리뷰를 작성자만 수정 가능
     */
    @PutMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewDto.Info> modifyReview(
            @PathVariable Long reviewId,
            @RequestBody @Valid ReviewModifyRequest request
            ) {

        // 유저
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();

        Review review = reviewService.modifyReview(user, reviewId, request);

        ReviewDto.Info reviewInfo = ReviewDto.fromEntityToReviewInfo(review);

        return ResponseEntity.ok().body(reviewInfo);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ReviewDto.Info> deleteReview(
            @PathVariable Long reviewId
    ) {
        // 유저
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();

        Review review = reviewService.deleteReview(user, reviewId);

        ReviewDto.Info reviewInfo = ReviewDto.fromEntityToReviewInfo(review);

        return ResponseEntity.ok().body(reviewInfo);

    }

    /**
     * 특정 매장 리뷰 조회
     */
    @GetMapping("/stores/{storeId}")
    @PreAuthorize("hasRole('USER')")
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
