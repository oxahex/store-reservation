package archive.oxahex.api.configuration;

import archive.oxahex.api.handler.JwtAccessDeniedHandler;
import archive.oxahex.api.handler.JwtAuthenticationEntryPoint;
import archive.oxahex.api.security.JwtFilter;
import archive.oxahex.api.security.TokenProvider;
import archive.oxahex.api.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity
    ) throws Exception {

        httpSecurity
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizedHttpRequests -> authorizedHttpRequests
                        .requestMatchers("/auth/signup", "/auth/signin").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtFilter(tokenProvider, redisUtil), UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}
