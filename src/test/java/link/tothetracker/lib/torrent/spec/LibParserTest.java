package link.tothetracker.lib.torrent.spec;

import link.tothetracker.lib.LibRuntimeException;
import link.tothetracker.lib.constant.ByteUnit;
import link.tothetracker.lib.torrent.T3tInfo;
import link.tothetracker.lib.torrent.file.FileNode;
import link.tothetracker.lib.torrent.file.T3tTree;
import link.tothetracker.lib.util.JsonUtil;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
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

    private static record FileInfo(String path, String name, int num, long length, String hash) {}

    // region 测试正常的文件读写

    private static Stream<Arguments> okTorrents() {
        return Stream.of(
                Arguments.of("Single File.torrent", "sample-[1].mkv", 1, 335544310L,
                        "7fa6e0cbeec4eb473e8e279fc5569ea48b27203b"),
                Arguments.of("Single File In Directory.torrent", "example", 1, 335544310L,
                        "8e10a07e4ce46e70a9fd62397e8900019af50217"),
                Arguments.of("Multi File In Directory.torrent", "example", 3, 335544310L * 3,
                        "69c32b00f8978d5322a44a3f28162e29c81fe1f5"),
                Arguments.of("Too Many File.torrent", "Urusei Yatsura BDBOX", 1663, 1502865423858L,
                        "5a80323d812ef13d664861c2f6c226b2290c3436"),
                Arguments.of("Huge Torrent File.torrent", "US.Open.2019.FEED.1080i.H264-HDCTV",
                        149, 2429062269180L, "2762c5aff0c5292af9d29f55f52d7f567dc181e5"),
                Arguments.of("complex qb utf8.torrent", "complex", 7, 335544310L * 7,
                        "7f4dc88fe515dfffa966a337836a1b07cbb3a85e")
        );
    }

    @ParameterizedTest
    @MethodSource("okTorrents")
    void test_read(String path, String name, int num, long length, String hash) {
        var fileInfo = new FileInfo(path, name, num, length, hash);
        okRead("/source/", fileInfo);
    }

    private T3tInfo okRead(String directory, FileInfo fileInfo) {
        var fileName = fileInfo.path;
        var input = read(directory, fileName);
        var info = LibParser.read(input, "example");

        var hash = info.hash();
        Assertions.assertEquals(fileInfo.hash, hash);
        System.out.format("路径: %s%n", fileName);
        System.out.format("哈希: %s%n", hash);

        var files = info.files();
        Assertions.assertEquals(fileInfo.num, files.getNum());
        Assertions.assertEquals(fileInfo.name, files.getName());

        // 文件树
        var root = files.getRoot();
        var print = T3tTree.print(root);

        var length = root.getLength();
        var size = root.getSize();

        System.out.format("文件大小: %s%n", root.getSize());

        Assertions.assertEquals(ByteUnit.styled(length), size);
        System.out.println("文件树:");
        System.out.println(print);
        System.out.println("...");
        Assertions.assertEquals(fileInfo.length, length);

        // 保存文件树 json
        validateNodeId(root);
        var json = JsonUtil.toJson(root);
        System.out.println(json);
        // 反序列化
        var des = JsonUtil.fromJson(json, FileNode.class);
        validateTwoFileNode(root, des);
        return info;
    }

    private void validateTwoFileNode(FileNode root, FileNode des) {
        Assertions.assertEquals(root.getSize(), des.getSize());
        Assertions.assertEquals(root.getType(), des.getType());
        Assertions.assertEquals(root.getName(), des.getName());

        var c1 = root.getChildren();
        var c2 = root.getChildren();

        if (Objects.isNull(c1) && Objects.isNull(c2)) {
            return;
        }
        if ((Objects.nonNull(c1) && Objects.nonNull(c2) && c1.size() == c2.size())) {
            var i1 = c1.iterator();
            var i2 = c2.iterator();
            while (i1.hasNext() && i2.hasNext()) {
                validateTwoFileNode(i1.next(), i2.next());
            }

            return;
        }

        Assertions.fail();
    }

    private void validateNodeId(FileNode root) {
        var queue = new LinkedList<FileNode>();
        var node = root;
        queue.push(node);

        var ids = new HashSet<Integer>();
        var max = -1;
        var dupe = false;
        while (!queue.isEmpty()) {
            node = queue.pop();
            var id = node.getId();
            if (id > max) {
                max = id;
            }
            if (!ids.add(id)) {
                dupe = true;
            }

            var children = node.getChildren();
            if (CollectionUtils.isNotEmpty(children)) {
                for (var child : node.getChildren()) {
                    queue.addLast(child);
                }
            }
        }

        Assertions.assertFalse(dupe);
        Assertions.assertEquals(max + 1, ids.size());
    }

    // endregion

    // region 测试异常文件

    private static Stream<Arguments> invalidTorrents() {
        return Stream.of(
                Arguments.of("single comet gbk.torrent", "only utf-8 encoding supported!"),
                Arguments.of("empty.torrent", "'length' must > 0!"),
                Arguments.of("miss.torrent", "'info' is not satisfactory!"),
                Arguments.of("broken.torrent", "unknown indicator!"),
                Arguments.of("dupe.torrent", "dupe name")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTorrents")
    void invalid(String path, String msg) {
        var fileInfo = new FileInfo(path, null, 1, 335544310L, null);
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
    void test_write(String path, String name, int num, long length, String hash) throws IOException {
        var fileInfo = new FileInfo(path, name, num, length, hash);
        var info = okRead("/source/", fileInfo);
        var hash1 = info.hash();

        var extra = new ExtraInfo("https://example.com", null);
        var bytes = LibParser.write(info, extra);

        var targetPath = "src/test/resources/out/" + path;
        var file = new File(targetPath);
        FileUtils.writeByteArrayToFile(file, bytes);

        var info2 = okRead("/out/", fileInfo);
        var hash2 = info2.hash();

        Assertions.assertEquals(hash1, hash2);
    }


    // endregion

}