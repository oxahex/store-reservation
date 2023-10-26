package archive.oxahex.api.security;

import archive.oxahex.domain.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class AuthUser {
    private Long id;
    private String email;
    private String role;

    public AuthUser(Long id, String email, String role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

    public static AuthUser fromEntityToAuthUser(User user) {
        return new AuthUser(
                user.getId(), user.getEmail(), user.getRole().toString()
        );
    }

}
