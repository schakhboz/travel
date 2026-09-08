package uz.insonline.travel.log.service;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static uz.insonline.travel.commons.util.JSONUtil.decodeUnicode;

@Component
public class ResponseLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger("res-logs");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest httpRequest) || !(response instanceof HttpServletResponse httpResponse)) {
            chain.doFilter(request, response);
            return;
        }

        // Retrieve the request ID
        String requestId = (String) httpRequest.getAttribute("request-id");

        CachedBodyHttpServletResponse cachedResponse = new CachedBodyHttpServletResponse((HttpServletResponse) response);

        chain.doFilter(request, cachedResponse);

        // Log response body with Unicode decoding
        byte[] body = cachedResponse.getBody();
        String contentType = cachedResponse.getContentType();
        boolean isBinary = contentType != null && (
                contentType.contains("pdf") ||
                contentType.contains("image") ||
                contentType.contains("octet-stream") ||
                contentType.contains("audio") ||
                contentType.contains("video") ||
                contentType.contains("zip")
        );

        String decodedResponseBody;
        if (isBinary) {
            decodedResponseBody = "[BINARY CONTENT (" + contentType + ", " + body.length + " bytes)]";
        } else {
            String responseBody = new String(body, cachedResponse.getCharacterEncoding() != null ? cachedResponse.getCharacterEncoding() : "UTF-8");
            decodedResponseBody = decodeUnicode(responseBody);
        }

        if (!shouldSkipLogging(httpRequest)) {
            // Log response ID
            logger.info("Request ID: {}", requestId);

            // Log response status
            int status = cachedResponse.getStatus();
            logger.info("Response Status: {}", status);

            // Log headers
            for (String headerName : cachedResponse.getHeaderNames()) {
                String headerValue = cachedResponse.getHeader(headerName);
                logger.info("Response Header: {} = {}", headerName, headerValue);
            }
            logger.info("Response Body: {}", decodedResponseBody);
        } else {
            logger.info("Response Body logging skipped for URL: {}", httpRequest.getRequestURI());
        }

        httpResponse.getOutputStream().write(body);
    }

    /**
     * Determines whether to skip logging based on request or response content.
     *
     * @param request       The HTTP request.
     * @return true if logging should be skipped, false otherwise.
     */
    private boolean shouldSkipLogging(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return requestURI.contains("/swagger") || requestURI.contains("/v3/api-docs");
    }
}
