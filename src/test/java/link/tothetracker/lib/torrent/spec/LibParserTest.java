package link.tothetracker.lib.torrent.spec;

import link.tothetracker.lib.LibRuntimeException;
import link.tothetracker.lib.constant.ByteUnit;
import link.tothetracker.lib.torrent.T3tInfo;
import link.tothetracker.lib.torrent.file.T3tTree;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * @author t3link
 */
class LibParserTest {

    @SneakyThrows
    private InputStream read(String directory, String fileName) {
        var path = directory + fileName;
        return LibParser.class.getResourceAsStream(path);
    }

    private static record FileInfo(String path, String name, int num, long length) {}

    // region 测试正常的文件读写

    private static Stream<Arguments> okTorrents() {
        return Stream.of(
                Arguments.of("Single File.torrent", "sample-[1].mkv", 1, 335544310L),
                Arguments.of("Single File In Directory.torrent", "example", 1, 335544310L),
                Arguments.of("Multi File In Directory.torrent", "example", 3, 335544310L * 3),
                Arguments.of("Too Many File.torrent", "Urusei Yatsura BDBOX", 1663, 1502865423858L),
                Arguments.of("Huge Torrent File.torrent", "US.Open.2019.FEED.1080i.H264-HDCTV", 149, 2429062269180L),
                Arguments.of("complex qb utf8.torrent", "complex", 7, 335544310L * 7)
        );
    }

    @ParameterizedTest
    @MethodSource("okTorrents")
    void test_read(String path, String name, int num, long length) {
        var fileInfo = new FileInfo(path, name, num, length);
        okRead("/source/", fileInfo);
    }

    T3tInfo okRead(String directory, FileInfo fileInfo) {
        var fileName = fileInfo.path;
        var input = read(directory, fileName);
        var info = LibParser.read(input, "example");

        var hash = info.hash();
        System.out.format("路径: %s%n", fileName);
        System.out.format("哈希: %s%n", hash);

        var files = info.files();
        Assertions.assertEquals(fileInfo.num, files.getNum());
        Assertions.assertEquals(fileInfo.name, files.getName());

        // 文件树
        var root = files.getRoot();
        var print = T3tTree.print(root);

        var length = root.getLength();
        var styledLength = root.getStyledLength();

        System.out.format("文件大小: %s%n", root.getStyledLength());

        Assertions.assertEquals(ByteUnit.styled(length), styledLength);
        System.out.println("文件树:");
        System.out.println(print);
        System.out.println("...");
        Assertions.assertEquals(fileInfo.length, length);

        return info;
    }

    // endregion

    // region 测试异常文件

    private static Stream<Arguments> invalidTorrents() {
        return Stream.of(
                Arguments.of("single comet gbk.torrent", "only utf-8 encoding supported!"),
                Arguments.of("empty.torrent", "'length' must > 0!"),
                Arguments.of("miss.torrent", "'info' is not satisfactory!"),
                Arguments.of("broken.torrent", "unknown indicator!")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTorrents")
    void invalid(String path, String msg) {
        var fileInfo = new FileInfo(path, null, 1, 335544310L);
        var fileName = fileInfo.path;
        var input = read("/source/invalid/", fileName);

        var success = true;
        try {
            LibParser.read(input, "empty");
        } catch (Exception e) {
            success = false;
            Assertions.assertTrue(e instanceof LibRuntimeException);
            var message = e.getMessage();

            Assertions.assertTrue(message.contains(msg));
        }
        Assertions.assertFalse(success);
    }

    // endregion

    // region 测试写入

    @ParameterizedTest
    @MethodSource("okTorrents")
    void test_write(String path, String name, int num, long length) throws IOException {
        var fileInfo = new FileInfo(path, name, num, length);
        var info = okRead("/source/", fileInfo);
        var hash = info.hash();

        var extra = new ExtraInfo("https://example.com", null);
        var bytes = LibParser.write(info, extra);

        var targetPath = "src/test/resources/out/" + path;
        var file = new File(targetPath);
        FileUtils.writeByteArrayToFile(file, bytes);

        var info2 = okRead("/out/", fileInfo);
        var hash2 = info2.hash();

        Assertions.assertEquals(hash, hash2);
    }


    // endregion

}