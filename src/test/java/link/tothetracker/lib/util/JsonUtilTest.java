package link.tothetracker.lib.util;

import link.tothetracker.lib.LibRuntimeException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author t3link
 */
class JsonUtilTest {

    @Data
    @NoArgsConstructor @AllArgsConstructor
    static class TmpEntity {
        private String field1;

        private String field2;
    }

    private TmpEntity entity;

    private String expected;

    @BeforeEach
    void before() {
        entity = new TmpEntity("value1", "value2");
        expected = "{\"field1\":\"value1\",\"field2\":\"value2\"}";
    }

    @Test
    void toJson() {
        var json = JsonUtil.toJson(entity);
        Assertions.assertEquals(expected, json);
    }

    @Test
    void fromJson() {
        var from = JsonUtil.fromJson(expected, TmpEntity.class);
        Assertions.assertEquals(entity, from);
    }

    @Test
    void fromJsonError() {
        try {
            var from = JsonUtil.fromJson("[[]]]]", TmpEntity.class);
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof LibRuntimeException);
        }
    }

    @Test
    void toNode() {
        var node = JsonUtil.toNode(entity);
        Assertions.assertEquals("value1", node.get("field1").textValue());
        Assertions.assertEquals("value2", node.get("field2").textValue());
    }

    @Test
    void isJsonString() {
        Assertions.assertTrue(JsonUtil.isJsonString(expected));
        Assertions.assertFalse(JsonUtil.isJsonString("abc"));
    }

}