package archive.oxahex.api.dto.response;

import archive.oxahex.api.dto.StoreDto;
import archive.oxahex.api.dto.UserDto;
import archive.oxahex.domain.entity.Store;
import archive.oxahex.domain.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreRegisterResponse {

    private UserDto.Info user;
    private StoreDto.Info store;

    public static StoreRegisterResponse fromEntityToResponse(User user, Store store) {
        StoreRegisterResponse response = new StoreRegisterResponse();
        response.setUser(UserDto.fromEntityToUserInfo(user));
        response.setStore(StoreDto.fromEntityToStoreInfo(store));

        return response;
    }
}
