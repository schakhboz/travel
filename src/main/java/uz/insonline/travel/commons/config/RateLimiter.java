package uz.insonline.travel.commons.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiter {
    private int MAX_REQUESTS;
    private long TIME_WINDOW;

    private final Map<String, Map<String, UserRequestData>> requestMap = new ConcurrentHashMap<>();

    @Autowired
    public void setMaxLimits(@Value("${ratelimit.MAX_REQUESTS}") int maxRequests,
                             @Value("${ratelimit.TIME_WINDOW}") long timeWindow) {
        this.MAX_REQUESTS = maxRequests;
        this.TIME_WINDOW = timeWindow;
    }

    public boolean isAllowed(String clientId, String method) {
        long currentTime = Instant.now().toEpochMilli();

        requestMap.putIfAbsent(clientId, new ConcurrentHashMap<>());
        Map<String, UserRequestData> methodDataMap = requestMap.get(clientId);

        methodDataMap.putIfAbsent(method, new UserRequestData(0, currentTime));
        UserRequestData requestData = methodDataMap.get(method);

        synchronized (requestData) {
            if (currentTime - requestData.timestamp > TIME_WINDOW) {
                requestData.requestCount = 1;
                requestData.timestamp = currentTime;
                return true;
            }

            if (requestData.requestCount < MAX_REQUESTS) {
                requestData.requestCount++;
                return true;
            }

            return false;
        }
    }

    private static class UserRequestData {
        private int requestCount;
        private long timestamp;

        public UserRequestData(int requestCount, long timestamp) {
            this.requestCount = requestCount;
            this.timestamp = timestamp;
        }
    }
}
