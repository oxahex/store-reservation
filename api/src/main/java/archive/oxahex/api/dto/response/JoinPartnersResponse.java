package archive.oxahex.api.dto.response;

import archive.oxahex.api.dto.PartnersDto;
import archive.oxahex.api.dto.request.JoinPartnersRequest;
import archive.oxahex.domain.entity.Partners;
import archive.oxahex.domain.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinPartnersResponse {

    private PartnersDto.Detail partners;
    private String token;

    public static JoinPartnersResponse fromEntityToResponse(
            User user, Partners partners, String token
    ) {
        JoinPartnersResponse response = new JoinPartnersResponse();
        response.setPartners(PartnersDto.fromEntityToPartnersDetail(partners, user));
        response.setToken(token);

        return response;
    }
}
