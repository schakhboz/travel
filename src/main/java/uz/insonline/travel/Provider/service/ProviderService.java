package uz.insonline.travel.Provider.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import uz.insonline.travel.Provider.payload.reponse.PersonBirthDateResponseDto;
import uz.insonline.travel.Provider.payload.request.PersonBirthDateRequestDto;
import uz.insonline.travel.authentication.entity.UserEntity;
import uz.insonline.travel.commons.exceptions.CreationException;
import uz.insonline.travel.log.service.LogService;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private final JdbcTemplate jdbcTemplate;
    private final LogService logService;

    @SneakyThrows
    public PersonBirthDateResponseDto personBirthDay(PersonBirthDateRequestDto dto) {
        checkClick();
        PersonBirthDateResponseDto t = new PersonBirthDateResponseDto();
        String query = String.format("SELECT " +
                        "ERROR," +
                        "ERROR_MESSAGE," +
                        "PINFL," +
                        "LAST_NAME," +
                        "FIRST_NAME," +
                        "MIDDLE_NAME," +
                        "LAST_NAME_ENG," +
                        "FIRST_NAME_ENG," +
                        "REGION_ID," +
                        "DISTRICT_ID," +
                        "ADDRESS," +
                        "GENDER," +
                        "TO_CHAR(BIRTH_DATE,'dd.mm.yyyy') as DATE_BIRTH" +
                        " FROM TABLE(get_passport_birth_date(to_date('%s','dd.mm.yyyy'),'%s','%s'))",
                dto.getBirthDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                dto.getPassportSeries(),
                dto.getPassportNumber());
        System.out.println(query);

        Map<String, Object> out = jdbcTemplate.queryForMap(query);

        Object errorVal = out.get("ERROR");
        if (errorVal != null) {
            t.setResult(Integer.parseInt(String.valueOf(errorVal)));
        }
        String errorMsg = getValue(out, "ERROR_MESSAGE");
        if (t.getResult() != 0) {
            t.setResult_message(errorMsg != null && !errorMsg.equals("-") && !errorMsg.trim().isEmpty()
                    ? errorMsg
                    : "Данные по предоставленным параметрам не найдены");
        } else {
            t.setResult_message(errorMsg != null && !errorMsg.equals("-") ? errorMsg : null);
        }
        t.setPinfl(getValue(out, "PINFL"));
        t.setLast_name(getValue(out, "LAST_NAME"));
        t.setFirst_name(getValue(out, "FIRST_NAME"));
        t.setMiddle_name(getValue(out, "MIDDLE_NAME"));
        t.setLast_name_eng(getValue(out, "LAST_NAME_ENG"));
        t.setFirst_name_eng(getValue(out, "FIRST_NAME_ENG"));
        t.setRegion_id(getValue(out, "REGION_ID"));
        t.setDistrict_id(getValue(out, "DISTRICT_ID"));
        t.setAddress(getValue(out, "ADDRESS"));
        t.setGender(getValue(out, "GENDER"));
        t.setBirth_date(getValue(out, "DATE_BIRTH"));
        logService.addLog("PROVIDER-PASSPORT-BIRTH-DATE", dto, t.getResult(), t.getResult_message(), t, null);

        if (t.getResult() != 0) {
            HttpStatus status = HttpStatus.BAD_REQUEST;
            String userFriendlyMsg = "Произошла ошибка при получении данных";
            if (errorMsg != null) {
                if (errorMsg.contains("404")) {
                    status = HttpStatus.NOT_FOUND;
                    userFriendlyMsg = "Данные по предоставленным параметрам не найдены";
                } else if (errorMsg.contains("503") || errorMsg.contains("Сетевая ошибка API") || errorMsg.contains("недоступен")) {
                    status = HttpStatus.SERVICE_UNAVAILABLE;
                    userFriendlyMsg = "Сервис временно недоступен";
                } else if (errorMsg.contains("500")) {
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                    userFriendlyMsg = "Внутренняя ошибка сервиса";
                } else if (errorMsg.contains("403")) {
                    status = HttpStatus.FORBIDDEN;
                    userFriendlyMsg = "Доступ запрещен";
                } else if (errorMsg.contains("401")) {
                    status = HttpStatus.UNAUTHORIZED;
                    userFriendlyMsg = "Не авторизован";
                }
            }
            t.setResult_message(userFriendlyMsg);
            throw new CreationException(t, status);
        }

        return t;
    }

    private String getValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? String.valueOf(val) : null;
    }

    @SneakyThrows
    private void checkClick(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity users = (UserEntity) authentication.getPrincipal();
        if (users.getTbId()==1160){
            throw new AccessDeniedException("This method is not allowed to access this resource");
        }
    }

}
