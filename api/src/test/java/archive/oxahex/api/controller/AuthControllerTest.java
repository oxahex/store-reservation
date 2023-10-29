package archive.oxahex.api.controller;

import archive.oxahex.api.dto.request.JoinRequest;
import archive.oxahex.api.dto.request.LoginRequest;
import archive.oxahex.api.security.AuthUser;
import archive.oxahex.api.service.AuthService;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.type.RoleType;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@AutoConfigureDataJpa
@AutoConfigureDataRedis
@ActiveProfiles("test")
@WebMvcTest(value = AuthController.class)
class AuthControllerTest {

    @MockBean
    public AuthService authService;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public MockMvc mockMvc;

    @Autowired
    public ObjectMapper objectMapper;


    @Test
    @DisplayName("새로운 회원을 등록한다.")
    void join() throws Exception {
        // given
        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setUsername("test1");
        joinRequest.setPassword("test1test1test1");
        joinRequest.setEmail("test1@gmail.com");
        joinRequest.setPhoneNumber("01011111111");

        User user = generateUserEntity("test1", RoleType.ROLE_USER);
        given(authService.createUser(any(JoinRequest.class))).willReturn(user);


        // when
        // then
        mockMvc
                .perform(MockMvcRequestBuilders.post("/auth/join")
                        .content(objectMapper.writeValueAsString(joinRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("test1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("test1@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    @DisplayName("Email과 Password 일치 시 Authorization Header에 JWT 토큰이 전송된다.")
    @Transactional
    void login_success() throws Exception {
        // given
        User user = generateUserEntity("test", RoleType.ROLE_USER);
        AuthUser authUser = new AuthUser(user);

        given(authService.loadUserByUsername(anyString()))
                .willReturn(authUser);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@gmail.com");
        loginRequest.setPassword("testtesttest");

        // when
        // then
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/auth/login")
                                .content(objectMapper.writeValueAsString(loginRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .with(SecurityMockMvcRequestPostProcessors.csrf())
                )
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().exists("Authorization"));
    }


    private User generateUserEntity(String name, RoleType role) {

        return User.builder()
                .name(name)
                .email(name + "@gmail.com")
                .password(passwordEncoder.encode(name + name + name))
                .phoneNumber("01011111111")
                .role(role)
                .registeredDate(LocalDateTime.now())
                .build();
    }
}