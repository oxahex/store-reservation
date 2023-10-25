package archive.oxahex.api.controller;

import archive.oxahex.api.dto.ReviewDto;
import archive.oxahex.api.service.ReviewService;
import archive.oxahex.domain.entity.Review;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 리뷰 작성
     * 예약 내역에서 이용이 확인된 경우에만 리뷰 작성 가능
     * TODO: 미작업
     */
    @PostMapping("/reviews/{storeId}")
    @PreAuthorize("hasRole('USER')")
    public Object addReview(
            Authentication authentication,
            @PathVariable Long storeId,
            @RequestBody @Valid ReviewDto.Request request
    ) {

        log.info("review auth={}", authentication.getAuthorities());

        log.info("ReviewController.addReview request={}", request);
        System.out.println(request.getContent());

        return request;
    }

    /**
     * 특정 매장 리뷰 조회
     */
    @GetMapping("/{storeId}")
    @PreAuthorize("hasRole('USER') or hasRole('PARTNERS')")
    public ResponseEntity<List<ReviewDto.Info>> getStoreReviews(
            @PathVariable Long storeId
    ) {

        List<Review> reviews = reviewService.getAllReviews(storeId);
        List<ReviewDto.Info> reviewInfos = reviews.stream()
                .map(ReviewDto::fromEntityToReviewInfo).toList();

        return ResponseEntity.ok().body(reviewInfos);
    }
}
