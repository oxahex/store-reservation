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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * JWT 권한이 없는 경우 예외 처리
     */
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        sendErrorResponse(response);
    }

    private void sendErrorResponse(
            HttpServletResponse response
    ) throws IOException {

        log.error("JwtAccessDenied Error");
        response.setCharacterEncoding("utf-8");
        response.setStatus(ErrorType.ACCESS_DENIED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorDto errorResponse = new ErrorDto(
                ErrorType.ACCESS_DENIED.getHttpStatus().value(),
                ErrorType.ACCESS_DENIED.getErrorMessage()
        );

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
