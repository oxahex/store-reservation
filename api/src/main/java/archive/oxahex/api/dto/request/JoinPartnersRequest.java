package archive.oxahex.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
public class JoinPartnersRequest {

    @NotBlank(message = "파트너스 이름을 작성해주세요.")
    @Length(min = 4, message = "파트너스 이름은 4자 이상 입력해주세요.")
    @Length(max = 100, message = "파트너스 이름은 100자 이하로 입력해주세요.")
    private String name;
}
