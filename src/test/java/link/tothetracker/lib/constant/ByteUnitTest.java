package link.tothetracker.lib.constant;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author t3link
 */
class ByteUnitTest {

    @Test
    void show() {
        var styled = "";
        var max = 6;
        for (int i = 0; i < max; i++) {
            var start = (long) Math.pow(1000, i);
            for (int p = 1; p < 1000; p++) {
                var length = start * p;
                styled = ByteUnit.styled(length);
                // System.out.format("length: %18s, styled: %s\n", length, styled);
            }
        }

        Assertions.assertEquals("887.29 PB", styled);
    }

    @Test
    void styled() {
        var lengths = List.of(
                1L,
                1000L,
                2000L,
                1000_000L,
                2000_000L,
                1000_000_000L,
                2000_000_000L,
                1000_000_000_000L,
                2000_000_000_000L,
                1_000_000_000_000_000L,
                2_000_000_000_000_000L,
                1_000_000_000_000_000_000L,
                2_000_000_000_000_000_000L
        );

        var expected = List.of(
                "1.00 B",
                "0.98 KB",
                "1.95 KB",
                "976.56 KB",
                "1.91 MB",
                "953.67 MB",
                "1.86 GB",
                "931.32 GB",
                "1.82 TB",
                "909.49 TB",
                "1.78 PB",
                "888.18 PB",
                "1.73 EB"
        );

        var results = lengths.stream().map(ByteUnit::styled).collect(Collectors.toList());
        Assertions.assertEquals(expected, results);
    }
}