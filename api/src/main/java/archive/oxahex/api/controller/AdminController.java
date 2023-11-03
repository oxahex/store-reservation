package archive.oxahex.api.controller;

import archive.oxahex.api.dto.ReviewDto;
import archive.oxahex.api.security.AuthUser;
import archive.oxahex.api.service.AdminService;
import archive.oxahex.domain.entity.Review;
import archive.oxahex.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 리뷰 삭제
     * 리뷰 작성자인지 확인하지 않고 삭제 처리
     * @param reviewId 삭제할 리뷰 ID
     * @return 삭제한 리뷰 정보
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<ReviewDto.Info> deleteReview(
            @PathVariable Long reviewId
    ) {
        // 유저
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();

        Review review = adminService.deleteReview(reviewId);

        ReviewDto.Info reviewInfo = ReviewDto.fromEntityToReviewInfo(review);

        return ResponseEntity.ok().body(reviewInfo);
    }
}
