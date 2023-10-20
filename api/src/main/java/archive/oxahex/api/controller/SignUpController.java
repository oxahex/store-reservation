package archive.oxahex.api.controller;

import archive.oxahex.api.dto.SignUpDto;
import archive.oxahex.api.dto.UserDto;
import archive.oxahex.api.service.AuthService;
import archive.oxahex.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SignUpController {

    private final UserService userService;
    private final AuthService authService;

    /**
     * 회원 가입 기능
     * 최초 회원 가입 시 모든 유저는 ROLE_USER 로 정의합니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<UserDto.Info> signUp(@RequestBody @Valid SignUpDto.Request request) {

        log.info("SignUpController.signUp request={}", request);

        return new ResponseEntity<>(userService.createUser(request), HttpStatus.OK);
    }
}
