package archive.oxahex.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class ReviewModifyRequest {

    @NotNull(message = "변경할 별점을 입력해주세요.")
    @Range(min = 0, max = 5)
    public Integer rating;

    @NotBlank(message = "변경할 리뷰 내용을 입력해주세요.")
    @Length(min = 10, max = 500, message = "리뷰는 최소 10자 이상, 500자 미만으로 입력할 수 있습니다.")
    public String content;

}
