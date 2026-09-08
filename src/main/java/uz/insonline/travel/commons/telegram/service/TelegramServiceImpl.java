package uz.insonline.travel.commons.telegram.service;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import uz.insonline.travel.commons.payload.response.ApiResponseAll;
import uz.insonline.travel.commons.telegram.TelegramProps;
import uz.insonline.travel.commons.telegram.dto.TelegramRequest;
import uz.insonline.travel.commons.util.JSONUtil;
import uz.insonline.travel.log.service.CachedBodyHttpServletRequest;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@Log4j2
@Component
public class TelegramServiceImpl implements TelegramService {

    private final RestTemplate restTemplate;

    private final HttpHeaders httpHeaders = new HttpHeaders();

    private final TelegramProps telegramProps;

    @Value("${server.url}")
    private String server;

    public TelegramServiceImpl(RestTemplate restTemplate5,
                               TelegramProps telegramProps) {
        this.restTemplate = createSslBypassRestTemplate();
        this.telegramProps = telegramProps;
    }

    private RestTemplate createSslBypassRestTemplate() {
        return new RestTemplate(new org.springframework.http.client.SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(java.net.HttpURLConnection connection, String httpMethod) throws java.io.IOException {
                if (connection instanceof javax.net.ssl.HttpsURLConnection httpsConnection) {
                    try {
                        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[]{
                                new javax.net.ssl.X509TrustManager() {
                                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) { }
                                }
                        };
                        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
                        sc.init(null, trustAllCerts, new java.security.SecureRandom());
                        httpsConnection.setSSLSocketFactory(sc.getSocketFactory());
                        httpsConnection.setHostnameVerifier((hostname, session) -> true);
                    } catch (Exception e) {
                        log.error("SSL Bypass failed", e);
                    }
                }
                super.prepareConnection(connection, httpMethod);
            }
        });
    }

    @Override
    public void sendErrorToTelegram(Throwable e, WebRequest request, ResponseEntity<?> response) {
        String requestURL = "N/A";
        String method = "N/A";
        Object responseBody;
        String requestBody = "N/A";
        String responseCode = "N/A";
        String username = "N/A";

        try {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            String stackTrace = stringWriter.toString();

            int maxStackTraceLength = 600;
            if (stackTrace.length() > maxStackTraceLength) {
                stackTrace = stackTrace.substring(0, maxStackTraceLength) + "\n... (Truncated)";
            }

            responseBody = response.getBody();
            responseCode = response.getStatusCode().toString().split(" ")[0];

            try {
                CachedBodyHttpServletRequest cachedRequest = getCachedBodyHttpServletRequest((ServletWebRequest) request);
                requestURL = cachedRequest.getRequestURL().toString();
                method = cachedRequest.getMethod();

                requestBody = cachedRequest.getCachedBodyAsString();
                requestBody = JSONUtil.decodeUnicode(requestBody);

                String authorization = cachedRequest.getHeader("Authorization");

                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
                    username = auth.getName();
                }
                
                if ("N/A".equals(username) && authorization != null && authorization.startsWith("Basic ")) {
                    try {
                        String base64Credentials = authorization.substring("Basic ".length());
                        String decoded = new String(Base64.getDecoder().decode(base64Credentials));
                        String[] parts = decoded.split(":", 2);
                        username = parts[0];
                    } catch (Exception ex) {
                        log.debug("Failed to decode Basic auth: {}", ex.getMessage());
                    }
                }
                
                if ("N/A".equals(username) && authorization != null && authorization.startsWith("Bearer ")) {

                    log.debug("Bearer token found but not processed by Spring Security");
                }

            } catch (Exception exception) {
                log.error("Error while fetching request details: {}", exception.getMessage());
            }

            String timeInTashkent = ZonedDateTime.now(ZoneId.of("Asia/Tashkent")).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

            String errorMessage = "N/A";
            if (responseBody instanceof ApiResponseAll apiResponse) {
                errorMessage = apiResponse.getResult_message();
            } else if (responseBody != null) {
                errorMessage = responseBody.toString();
            }

            String messageBuilder = String.format(
                    """
                            🧩 Микросервис: <b>%s</b>
                            
                            ⚙️ Метод: <b>%s</b>
                            
                            🌐 Статус код: <b>%s</b>
                            
                            🔗 Эндпойнт: <b>%s</b>
                            
                            👤 Пользователь: <b>%s</b>
                            
                            ⏱️ Время: <b>%s</b>
                            
                            📨 Запрос: 
                            <pre><code class="language-json">%s</code></pre>
                            
                            📩 Респонс: 
                            <pre><code class="language-json">%s</code></pre>
                            
                            ❌ Причина ошибки: <b>%s</b>
                            """,
                    escapeHtml(server),
                    escapeHtml(method),
                    escapeHtml(responseCode),
                    escapeHtml(requestURL),
                    escapeHtml(username),
                    escapeHtml(timeInTashkent),
                    escapeHtml(requestBody),
                    escapeHtml(JSONUtil.convertObjectToJson(responseBody)),
                    escapeHtml(errorMessage)
            );


            TelegramRequest telegramRequest = new TelegramRequest(messageBuilder);
            System.out.println(telegramRequest);
            sendMessage(telegramRequest);

        } catch (Exception ex) {
            log.error("Error while sending error to telegram: {} ", ex.getMessage(), ex);
        }
    }

    private static CachedBodyHttpServletRequest getCachedBodyHttpServletRequest(ServletWebRequest request) throws IOException {
        HttpServletRequest httpRequest = request.getRequest();

                /*while (httpRequest instanceof HttpServletRequestWrapper) {
                    httpRequest = (HttpServletRequest) ((HttpServletRequestWrapper) httpRequest).getRequest();
                }*/

        HttpServletRequest requestToUse = httpRequest instanceof CachedBodyHttpServletRequest
                ? httpRequest
                : new CachedBodyHttpServletRequest(httpRequest);

        CachedBodyHttpServletRequest cachedRequest = (CachedBodyHttpServletRequest) requestToUse;
        return cachedRequest;
    }

    @PostConstruct
    void init() {
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    public void sendMessage(TelegramRequest request) {

        for (var chat : telegramProps.getChats().keySet()) {
            try {
                String chatId = telegramProps.getChats().get(chat);
                log.info("sending telegram message to: id:{}, name:{}", chatId, chat);
                String telegramUrl = String.format("https://api.telegram.org/bot%s/sendMessage", telegramProps.getToken());

                Map<String, String> requestBody = Map.of("chat_id", chatId, "text", request.getMessage(), "parse_mode", "HTML");
                HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, httpHeaders);

                restTemplate.exchange(telegramUrl, HttpMethod.POST, requestEntity, String.class);
            } catch (Exception ex) {
                log.error(ex);
            }
        }
    }

    private String escapeHtml(String text) {
        if (text == null) return "null";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

}
