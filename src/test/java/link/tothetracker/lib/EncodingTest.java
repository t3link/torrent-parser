package link.tothetracker.lib;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;

/**
 * @author t3link
 */
class EncodingTest {

    private static final Charset GBK = Charset.forName("GBK");

    @Test
    void gbk() {
        var str = "\uD83D\uDC08 dá nàng これまでは 동쪽에서 Österreich أهلاً بك في ";
        var bytes = str.getBytes(GBK);

        var str2 = new String(bytes, GBK);
        System.out.println(str2);

        // 编码后数据丢失
        Assertions.assertNotEquals(str, str2);
    }

}
