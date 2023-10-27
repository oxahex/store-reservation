package archive.oxahex.api.handler;

import archive.oxahex.api.dto.ErrorDto;
import archive.oxahex.api.exception.ErrorType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Email, Password 로그인 실패 시 예외 처리
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        sendErrorResponse(response);

    }

    private void sendErrorResponse(
            HttpServletResponse response
    ) throws IOException {

        log.error("JwtAuthentication Error");
        response.setCharacterEncoding("utf-8");
        response.setStatus(ErrorType.AUTHENTICATION_FAILURE.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorDto errorResponse = new ErrorDto(
                ErrorType.AUTHENTICATION_FAILURE.getHttpStatus().value(),
                ErrorType.AUTHENTICATION_FAILURE.getErrorMessage()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
