package archive.oxahex.api.mock;

import archive.oxahex.api.security.AuthUser;
import archive.oxahex.domain.entity.User;
import archive.oxahex.domain.type.RoleType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.List;

public class WithMockUserSecurityContextFactory implements WithSecurityContextFactory<WithMockUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockUser annotation) {
        String email = annotation.email();
        RoleType role = annotation.role();

        AuthUser authUser = new AuthUser(
                User.builder()
                        .email(email)
                        .role(role)
                        .build()
        );

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        authUser, null, authUser.getAuthorities()
                );

        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);

        return context;
    }
}
