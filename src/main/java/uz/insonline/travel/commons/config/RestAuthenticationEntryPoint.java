package uz.insonline.travel.commons.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uz.insonline.travel.authentication.payload.response.ApiResponseAll;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        String resultMessage = getResultMessage(authException);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        ApiResponseAll errorResponse = new ApiResponseAll(-1,
                "Authentication failed: " + resultMessage);

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(errorResponse));
    }

    private static String getResultMessage(AuthenticationException authException) {
        String resultMessage;

        if (authException instanceof BadCredentialsException) {
            resultMessage = "Invalid username or password.";
        } else if (authException instanceof InsufficientAuthenticationException) {
            resultMessage = "Authentication is required to access this resource.";
        } else if (authException instanceof LockedException) {
            resultMessage = "Your account is locked. Please contact support.";
        } else if (authException instanceof DisabledException) {
            resultMessage = "Your account is disabled. Please contact support.";
        } else if (authException instanceof UsernameNotFoundException) {
            resultMessage = "Invalid username or password";
        } else {
            resultMessage = "Authentication failed. Please check your credentials.";
        }
        return resultMessage;
    }

}
