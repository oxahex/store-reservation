package archive.oxahex.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreRegisterRequest {

    @NotBlank(message = "매장 이름을 입력해주세요.")
    private String name;

    @NotBlank(message = "매장 주소를 입력해주세요.")
    private String address;

    @NotBlank(message = "매장 설명을 입력해주세요.")
    private String description;

    @NotNull(message = "사용 가능한 테이블 수를 입력해주세요.")
    private Integer tableCount;

    @NotBlank(message = "해당 매장의 사업자 등록 번호를 입력해주세요")
    private String businessNumber;

}
