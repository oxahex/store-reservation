package archive.oxahex.api.configuration;

import archive.oxahex.api.handler.JwtAccessDeniedHandler;
import archive.oxahex.api.handler.JwtAuthenticationEntryPoint;
import archive.oxahex.api.security.JwtAuthenticationFilter;
import archive.oxahex.api.security.JwtAuthorizationFilter;
import archive.oxahex.api.security.TokenProvider;
import archive.oxahex.api.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenProvider tokenProvider;
    private final AuthService authService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .authorizeHttpRequests(authorizedHttpRequests -> authorizedHttpRequests
                        .requestMatchers("/auth/join", "/auth/login").permitAll()
                        .anyRequest().authenticated());

        http
                .addFilterBefore(
                        new JwtAuthenticationFilter(authenticationManager(), tokenProvider, "/auth/login"),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterBefore(
                        new JwtAuthorizationFilter(authenticationManager(), authService, tokenProvider),
                        UsernamePasswordAuthenticationFilter.class
                );

        // 인증, 인가 Custom Exception Handler
        http.exceptionHandling(exceptionHandler -> {
            exceptionHandler.authenticationEntryPoint(jwtAuthenticationEntryPoint);
            exceptionHandler.accessDeniedHandler(jwtAccessDeniedHandler);
        });


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(authService);
        provider.setPasswordEncoder(passwordEncoder());
        return new ProviderManager(provider);
    }
}
