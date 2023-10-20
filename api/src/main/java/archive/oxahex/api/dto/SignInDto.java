package archive.oxahex.api.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

public class SignInDto {

    @Getter
    @Setter
    static class Request {

        @NotBlank(message = "이메일을 입력해주세요.")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$", message = "올바른 이메일 형식이 아닙니다.")
        public String email;

        @NotBlank(message = "비밀번호를 입력해주세요.")
        @Min(value = 8, message = "비밀번호는 최소 8자 이상 입력해주세요.")
        public String password;

    }

    static class Response {

    }
}
