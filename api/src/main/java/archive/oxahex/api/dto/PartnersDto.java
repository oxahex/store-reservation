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

    public static Info fromEntityToPartnersInfo(Partners partners) {
        Info partnersInfo = new PartnersDto.Info();
        partnersInfo.setId(partners.getId());
        partnersInfo.setName(partners.getName());

        return partnersInfo;
    }

    @Getter
    @Setter
    public static class Detail {
        private String name;
        private UserDto.Info user;
    }
    public static Detail fromEntityToPartnersDetail(Partners partners, User user) {
        Detail partnersDetail = new Detail();
        partnersDetail.setName(partners.getName());
        partnersDetail.setUser(UserDto.fromEntityToUserInfo(user));

        return partnersDetail;
    }
}
