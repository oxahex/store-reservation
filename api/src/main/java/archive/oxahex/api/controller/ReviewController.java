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
@PreAuthorize("hasRole('USER')")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final AuthService authService;

    /**
     * 리뷰 작성
     * <ol>
     *     <li>예약 내역에서 이용이 확인된 경우에만 리뷰 작성 가능</li>
     *     <li>예약자 본인인 경우에만 리뷰 작성 가능</li>
     * </ol>
     */
    @PostMapping("/reservations/{reservationId}")
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
     * <ol>
     *     <li>해당 리뷰 작성자만 수정 가능</li>
     * </ol>
     */
    @PutMapping("/{reviewId}")
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

    /**
     * 리뷰 삭제
     * <ol>
     *     <li>리뷰 작성자만 삭제 가능</li>
     *     <li>관리자의 경우 리뷰 삭제가 가능하나, 다른 컨트롤러로 분리</li>
     * </ol>
     * @param reviewId 삭제할 리뷰 ID
     * @return 삭제한 리뷰 정보
     */
    @DeleteMapping("/{reviewId}")
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
    public ResponseEntity<List<ReviewDto.Info>> getStoreReviews(
            @PathVariable Long storeId
    ) {

        List<Review> reviews = reviewService.getAllReviews(storeId);
        List<ReviewDto.Info> reviewInfos = reviews.stream()
                .map(ReviewDto::fromEntityToReviewInfo).toList();

        return ResponseEntity.ok().body(reviewInfos);
    }
}
