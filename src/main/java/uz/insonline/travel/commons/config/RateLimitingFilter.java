package uz.insonline.travel.commons.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RateLimitingFilter implements Filter {

    final RateLimiter rateLimiter;
    final ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientId = httpRequest.getRemoteAddr();
        String method = httpRequest.getMethod();

        if (!rateLimiter.isAllowed(clientId,method)) {
            httpResponse.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            httpResponse.setContentType("application/json");
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("result", 405);
            responseBody.put("result_message", "Too many requests. Please try again later.");
            httpResponse.getWriter().write(objectMapper.writeValueAsString(responseBody));
            return;
        }

        chain.doFilter(request, response);
    }
}
