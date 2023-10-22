package archive.oxahex.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

public class PartnersDto {

    @Getter
    @Setter
    public static class Request {

        @NotBlank(message = "사업자 번호를 입력해주세요.")
        @Length(min = 10, max = 10, message = "올바른 사업자 번호가 아닙니다.")
        private String businessRegistrationNumber;
    }

    @Getter
    @Setter
    public static class Response {
        private UserDto.Info user;
        private String token;
    }
}
