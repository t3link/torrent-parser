package link.tothetracker.lib.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.text.DecimalFormat;

/**
 * @author t3link
 */
@AllArgsConstructor
@Getter
public enum ByteUnit {

    /**
     * byte
     */
    B(1, " B"),

    /**
     * Kilobyte
     */
    KB(1024, " KB"),

    /**
     * Megabyte
     */
    MB(Math.multiplyExact(KB.length, KB.length), " MB"),

    /**
     * Gigabyte
     */
    GB(Math.multiplyExact(KB.length, MB.length), " GB"),

    /**
     * Terabyte
     */
    TB(Math.multiplyExact(KB.length, GB.length), " TB"),

    /**
     * Petabyte
     */
    PB(Math.multiplyExact(KB.length, TB.length), " PB"),

    /**
     * Exabyte
     */
    EB(Math.multiplyExact(KB.length, PB.length), " EB"),

    ;

    /**
     * 包含多少字节
     */
    private final long length;

    private final String alias;

    /**
     * 需要进位显示的因子
     */
    private static final double FACTOR = (double) 1000 / 1024;

    /**
     * 文件大小转换成 -h (long的bytes格式化)
     */
    public static String styled(long length) {
        var units = values();
        // 从 KB -> PB 计算，跳过第一个 B
        for (var i = 1; i < units.length; i++) {
            var unit = units[i];
            if (length < (unit.length * FACTOR)) {
                return styled0(length, units[i - 1]);
            }
        }

        // 最后兜底
        return styled0(length, EB);
    }

    private static String styled0(long length, ByteUnit unit) {
        var df = new DecimalFormat("0.00");
        var cal = (double) length / unit.length;
        return df.format(cal) + unit.alias;
    }
}
