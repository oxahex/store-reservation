package archive.oxahex.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

public class SignUpDto {

    @Getter
    @Setter
    public static class Request {

        @NotBlank(message = "유저 이름을 입력해주세요.")
        private String username;

        @NotBlank(message = "로그인에 사용할 비밀번호를 입력해주세요.")
        @Length(min = 8, message = "비밀번호는 최소 8자 이상 입력해주세요.")
        private String password;

        @NotBlank(message = "휴대폰 번호를 입력해주세요.")
        @Length(min = 11, max = 11, message = "올바른 휴대폰 번호 형식이 아닙니다.")
        private String phoneNumber;

        @NotBlank(message = "로그인에 사용할 이메일을 입력해주세요.")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+.[A-Za-z]{2,6}$", message = "올바른 이메일 형식이 아닙니다.")
        private String email;
    }
}
