package archive.oxahex.api.mock;

import archive.oxahex.domain.type.RoleType;
import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUserSecurityContextFactory.class)
public @interface WithMockUser {

    String email() default "test@gmail.com";
    RoleType role() default RoleType.ROLE_USER;
}
