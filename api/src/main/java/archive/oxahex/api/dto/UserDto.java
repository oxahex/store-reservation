package archive.oxahex.api.dto;

import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.type.RoleType;
import lombok.Getter;
import lombok.Setter;



public class UserDto {

    @Getter
    @Setter
    public static class Info {
        private String username;
        private String email;
        private RoleType role;
    }

    public static UserDto.Info fromEntityToUserInfo(User user) {
        UserDto.Info userInfo = new UserDto.Info();
        userInfo.setUsername(user.getName());
        userInfo.setRole(user.getRole());
        userInfo.setEmail(user.getEmail());

        return userInfo;
    }
}
