package archive.oxahex.api.dto;

import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

public class PartnersDto {

    @Getter
    @Setter
    public static class Info {
        private Long id;
        private String name;
    }

    @Getter
    @Setter
    public static class Detail {
        private String name;
        private UserDto.Info user;
    }

    @Getter
    @Setter
    public static class Request {

        @NotBlank(message = "파트너스 이름을 작성해주세요.")
        @Length(min = 4, message = "파트너스 이름은 4자 이상 입력해주세요.")
        @Length(max = 100, message = "파트너스 이름은 100자 이하로 입력해주세요.")
        private String name;
    }

    @Getter
    @Setter
    public static class Response {
        private Detail partners;
        private String token;
    }

    public static Info fromEntityToPartnersInfo(Partners partners) {
        Info partnersInfo = new PartnersDto.Info();
        partnersInfo.setId(partners.getId());
        partnersInfo.setName(partners.getName());

        return partnersInfo;
    }

    public static Detail fromEntityToPartnersDetail(Partners partners, User user) {
        Detail partnersDetail = new Detail();
        partnersDetail.setName(partners.getName());
        partnersDetail.setUser(UserDto.fromEntityToUserInfo(user));

        return partnersDetail;
    }

    public static Response fromEntityToPartnersResponse(
            User user, Partners partners, String token
    ) {
        PartnersDto.Response partnersResponse = new PartnersDto.Response();
        partnersResponse.setPartners(PartnersDto.fromEntityToPartnersDetail(partners, user));
        partnersResponse.setToken(token);

        return partnersResponse;
    }
}
