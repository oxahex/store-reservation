package archive.oxahex.api.controller;

import archive.oxahex.api.dto.ReviewDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReviewController {

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

}
