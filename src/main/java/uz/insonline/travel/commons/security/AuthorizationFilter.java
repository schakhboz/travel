package uz.insonline.travel.commons.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import uz.insonline.travel.authentication.service.AuthDetailsService;
import uz.insonline.travel.commons.config.Md5Encoder;
import uz.insonline.travel.commons.config.RestAuthenticationEntryPoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class AuthorizationFilter extends OncePerRequestFilter {
    final JwtProvider jwtProvider;
    final AuthDetailsService userDetailsService;
    final Md5Encoder md5Encoder;
    final RestAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String authorizationHeader = request.getHeader("Authorization");
            if (StringUtils.hasText(authorizationHeader)) {
                if (authorizationHeader.startsWith("Bearer ")) {
                    String token = authorizationHeader.substring(7);
                    if (jwtProvider.validateToken(token)) {
                        String username = jwtProvider.getUsernameFromToken(token);
                        if (username != null) {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        }
                    }
                } else if (authorizationHeader.startsWith("Basic")) {
                    String base64Credentials = authorizationHeader.substring(6);
                    String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
                    String[] values = credentials.split(":", 2);
                    if (values.length == 2) {
                        String username = values[0];
                        String password = values[1];
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        if (md5Encoder.encode(password).equals(userDetails.getPassword())) {
                            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            if (auth.isAuthenticated()) {
                                SecurityContextHolder.getContext().setAuthentication(auth);
                            }
                        }
                    }
                }
            }
            chain.doFilter(request, response);
        } catch (AuthenticationException ex) {
            authenticationEntryPoint.commence(request, response, ex);
        }
    }

}
