package archive.oxahex.api.service;

import archive.oxahex.api.exception.CustomException;
import archive.oxahex.api.exception.ErrorType;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.repository.PartnersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PartnersServiceTest {

    @InjectMocks
    PartnersService partnersService;

    @Mock
    PartnersRepository partnersRepository;


    @Test
    @DisplayName("해당 유저가 등록한 파트너스가 없는 경우 PARTNERS_NOT_FOUND 예외를 던진다.")
    void getPartners_failure() {
        // given
        User user = User.builder()
                .email("test@gmail.com").build();

        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.empty());

        // when
        CustomException exception = assertThrows(CustomException.class,
                () ->  partnersService.getPartners(user));

        // then
        assertEquals(ErrorType.PARTNERS_NOT_FOUND.getErrorMessage(), exception.getErrorMessage());
        assertEquals(ErrorType.PARTNERS_NOT_FOUND.getHttpStatus(), exception.getHttpStatus());

    }

    @Test
    @DisplayName("해당 유저가 등록한 파트너스가 있는 경우 유저의 파트너스 정보를 반환한다.")
    void getPartners_success() {

        // given
        User user = User.builder()
                .email("test@gmail.com").build();
        Partners partners = Partners.builder()
                        .name("파트너스 이름").build();
        partners.setUser(user);

        given(partnersRepository.findByUser(any(User.class)))
                .willReturn(Optional.of(partners));

        // when
        Partners findPartners = partnersService.getPartners(user);

        // then
        assertEquals(findPartners.getUser().getEmail(), user.getEmail());
        assertEquals(findPartners.getName(), "파트너스 이름");
    }
}