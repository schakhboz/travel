package uz.insonline.travel.log.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.insonline.travel.authentication.entity.UserEntity;
import uz.insonline.travel.authentication.repository.LogRepository;
import uz.insonline.travel.log.entity.LogEntity;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LogService {
    final ObjectMapper objectMapper;
    final LogRepository logRepository;
    String desiredTimeZone = "Asia/Tashkent";

    ZonedDateTime zonedDateTime = ZonedDateTime.now(TimeZone.getTimeZone(desiredTimeZone).toZoneId());

    @SneakyThrows
    private String convertObjectToJson(Object obj) {
        return objectMapper.writeValueAsString(obj);
    }

    @Transactional
    public void addLog(String method, Object request, Integer resultCode, String responseMessage, Object response, Long anketaId) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserEntity users = (UserEntity) authentication.getPrincipal();

            LogEntity log = new LogEntity();
            log.setMethod(method);
            log.setLogDate(Date.from(zonedDateTime.toInstant()));
            log.setReqContent(convertObjectToJson(request));
            log.setResCode(resultCode);
            log.setResMessage(responseMessage);
            log.setResContent(convertObjectToJson(response));
            log.setAnketaId(anketaId);
            log.setUserId(users.getTbId());
            log.setMicroservice("api-travel");
            log.setTimeSpent(System.currentTimeMillis());
            log.setProductTypeId(17);
            logRepository.save(log);
        } catch (Exception e) {
            log.warn("Unable to write log to LOG_API_SERVER: {}", e.getMessage());
        }
    }

    @Transactional
    public void addLog(String method, Object request, Integer resultCode, String responseMessage, Long anketaId) {

        try {
            LogEntity log = new LogEntity();
            log.setMethod(method);
            log.setLogDate(Date.from(zonedDateTime.toInstant()));
            log.setReqContent(convertObjectToJson(request));
            log.setResCode(resultCode);
            log.setResMessage(responseMessage);
            log.setAnketaId(anketaId);
            log.setMicroservice("api-travel");
            log.setTimeSpent(System.currentTimeMillis());
            log.setProductTypeId(17);
            logRepository.save(log);
        } catch (Exception e) {
            log.warn("Unable to write log to LOG_API_SERVER: {}", e.getMessage());
        }
    }
}
