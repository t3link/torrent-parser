package link.tothetracker.lib.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import link.tothetracker.lib.LibRuntimeException;
import lombok.extern.log4j.Log4j2;

/**
 * @author t3link
 */
@Log4j2
public final class JsonUtil {

    public static final String DEFAULT_TIME_ZONE = "GMT+8";

    public static final String DEFAULT_DATE_STYLE = "yyyy-MM-dd HH:mm:ss";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(SerializationFeature.INDENT_OUTPUT, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private JsonUtil() { }

    public static <T> String toJson(T entity) {
        try {
            return MAPPER.writeValueAsString(entity);
        } catch (JsonProcessingException ex) {
            throw new LibRuntimeException(ex);
        }
    }

    public static <T> T fromJson(String json, Class<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException ex) {
            throw new LibRuntimeException(ex);
        }
    }

    public static <T> JsonNode toNode(T entity) {
        return MAPPER.valueToTree(entity);
    }

    public static boolean isJsonString(String json) {
        try {
            MAPPER.readTree(json);
            return true;
        } catch (Exception var2) {
            if (log.isDebugEnabled()) {
                log.debug("invalid json: \n{}", json, var2);
            }
            return false;
        }
    }

}
