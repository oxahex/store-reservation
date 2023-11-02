package archive.oxahex.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreModifyRequest {

    @NotBlank(message = "변경할 이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "변경할 주소를 입력해주세요.")
    private String address;

    @NotBlank(message = "변경할 매장 설명을 입력해주세요.")
    private String description;

    @NotNull(message = "사용 가능한 테이블 수를 입력해주세요.")
    private Integer tableCount;
}
