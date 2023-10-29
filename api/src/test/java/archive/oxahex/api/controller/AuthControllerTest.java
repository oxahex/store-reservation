package archive.oxahex.api.controller;

import archive.oxahex.api.dto.SignUpDto;
import archive.oxahex.api.security.TokenProvider;
import archive.oxahex.api.service.AuthService;
import archive.oxahex.domain.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthService authService;

    @Mock
    private TokenProvider tokenProvider;


    @Spy
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void joinSuccessText() throws Exception {

        // given
        SignUpDto.Request request = getSignUpRequest();
        User user = getUserEntity();
        Mockito.doReturn(user).when(authService).createUser(request);

        // when
        // then
    }

    @Test
    @DisplayName("회원가입 - 실패 - Email 중복")
    void joinFailText() throws Exception {
        // given
        mockMvc
                .perform(
                        post("/auth/join")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(getSignUpRequest())))
                .andDo(print())
                .andExpect(status().isConflict());

        // when
        // then
    }

    private SignUpDto.Request getSignUpRequest() {
        SignUpDto.Request request = new SignUpDto.Request();
        request.setEmail("test@gmail.com");
        request.setPhoneNumber("01011111111");
        request.setUsername("test");
        request.setPassword("testtesttest");

        return request;
    }

    private User getUserEntity() {
        User user = new User();
        user.setId(0L);
        user.setName("test");
        user.setEmail("test@gmail.com");
        user.setPassword("testtesttest");
        user.setPhoneNumber("10111111111");

        return user;
    }



}