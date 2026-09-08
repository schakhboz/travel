package uz.insonline.travel.commons.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JSONUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static String convertObjectToJson(Object object) throws JsonProcessingException {
        objectMapper.findAndRegisterModules();
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Decodes escaped Unicode characters in a JSON string.
     *
     * @param responseBody The original response body.
     * @return The decoded response body.
     */
    public static String decodeUnicode(String responseBody) {
        try {
            Object json = objectMapper.readValue(responseBody, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception ignored) {
        }
        return responseBody;
    }
}
