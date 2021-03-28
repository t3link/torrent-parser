package link.tothetracker.lib.torrent.spec;

import link.tothetracker.lib.LibRuntimeException;
import link.tothetracker.lib.constant.ByteUnit;
import link.tothetracker.lib.encode.BeDecoder;
import link.tothetracker.lib.encode.type.AbstractBeValue;
import link.tothetracker.lib.encode.type.ByteStringBeValue;
import link.tothetracker.lib.encode.type.DictionaryBeValue;
import link.tothetracker.lib.encode.type.ListBeValue;
import link.tothetracker.lib.encode.type.NumberBeValue;
import link.tothetracker.lib.torrent.T3tInfo;
import link.tothetracker.lib.torrent.file.T3tFile;
import link.tothetracker.lib.torrent.file.T3tFiles;
import link.tothetracker.lib.torrent.file.T3tTree;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

/**
 * @author t3link
 */
public final class LibParser {

    private LibParser() {}

    /**
     * 解析一个种子为数据库存储的格式
     */
    public static T3tInfo read(InputStream in, String source) {
        try (in) {
            var value = BeDecoder.decode(in);
            if (!(value instanceof DictionaryBeValue dict)) {
                throw new LibRuntimeException("invalid torrent file!");
            }

            // 首先确定文件编码
            if (!supported(dict)) {
                throw new LibRuntimeException("only utf-8 encoding supported!");
            }
            // 种子 info
            var info = dict.get(Key.INFO);
            var files = INFO.validate(info);
            // 生成本站的 hash
            return hash(info,source, files);
        } catch (LibRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new LibRuntimeException("解析异常", e);
        }
    }

    /**
     * 重新生成种子文件
     */
    public static byte[] write(T3tInfo info, ExtraInfo extra) {
        // header announce 、 create by 、encoding
        var fields = new TreeMap<ByteStringBeValue, AbstractBeValue>();
        fields.put(Key.ANNOUNCE,
                new ByteStringBeValue(extra.getAnnounce())
        );
        fields.put(Key.ENCODING,
                new ByteStringBeValue(StandardCharsets.UTF_8.name())
        );

        // info 域直接克隆现成的 bytes
        var dict = new DictionaryBeValue();
        dict.clone(info.bytes());
        fields.put(Key.INFO, dict);

        // 站点的用户信息等
        fields.put(Key.SITE_INFO, extra);
        var root = new DictionaryBeValue(fields);
        return root.write();
    }

    // region 基本方法

    private static boolean supported(DictionaryBeValue dict) {
        var bev = dict.get(Key.NAME_UTF8);
        if (Objects.nonNull(bev)) {
            return false;
        }

        bev = dict.get(Key.ENCODING);
        if (Objects.isNull(bev)) {
            return true;
        }

        if (!(bev instanceof ByteStringBeValue value)) {
            throw new LibRuntimeException("invalid encoding byte string!");
        }

        var name = value.toString();
        try {
            return Objects.equals(StandardCharsets.UTF_8, Charset.forName(name));
        } catch (Exception ignored) {
            throw new LibRuntimeException("invalid encoding! " + name);
        }
    }

    private static T3tInfo hash(AbstractBeValue bev, String source, T3tFiles files) {
        // 列表中的 File 元素
        if (!(bev instanceof DictionaryBeValue dict)) {
            throw new LibRuntimeException("'info' is not List of Dictionaries!");
        }

        // 追加 private
        dict.put(Key.PRIVATE, new NumberBeValue(1L));
        // 追加站点私有属性 防止一种多上报 tracker
        dict.put(Key.SOURCE, new ByteStringBeValue(source.getBytes(StandardCharsets.UTF_8)));
        var data = dict.write();
        var hash = DigestUtils.sha1Hex(data);
        return new T3tInfo(data, hash, files);
    }

    private static T3tFile file(AbstractBeValue bev) {
        // 列表中的 File 元素
        if (!(bev instanceof DictionaryBeValue dict)) {
            throw new LibRuntimeException("'files' is not List of Dictionaries!");
        }
        List<String> path = Collections.emptyList();
        var length = -1L;
        // File 中的键值对
        var iterator = dict.iterator();
        while (iterator.hasNext()) {
            var fileNext = iterator.next();
            var key = fileNext.getKey();
            var value = fileNext.getValue();
            // 去除 md5 等无用字段
            if (Key.LENGTH.equals(key)) {
                // check
                length = LENGTH.validate(value);
            } else if (Key.PATH.equals(key)) {
                // check
                path = PATH.validate(value);
            } else {
                iterator.remove();
            }
        }

        if (CollectionUtils.isEmpty(path)) {
            throw new LibRuntimeException("'path' is not List");
        }
        if (length < 0L) {
            throw new LibRuntimeException("'length' is invalid");
        }

        return new T3tFile(length, path);
    }

    // endregion

    // region 校验规则

    private static final Validator<Void> PIECE_LENGTH = bev -> {
        // piece length must be number
        if (!(bev instanceof NumberBeValue value)) {
            throw new LibRuntimeException("'piece length' is not Number!");
        }
        // piece length is always 2^N
        var pieceLength = value.getValue();
        if (pieceLength <= 0) {
            throw new LibRuntimeException("'piece length' is not 2^N!");
        }
        while (pieceLength % 2 == 0) {
            pieceLength = pieceLength / 2;
        }
        if (pieceLength != 1) {
            throw new LibRuntimeException("'piece length' is not 2^N!");
        }

        return null;
    };

    private static final Validator<Void> PIECES = bev -> {
        if (!(bev instanceof ByteStringBeValue value)) {
            throw new LibRuntimeException("'pieces' is not Byte Strings!");
        }
        // pieces length is always divided 20
        if (value.getValue().length % 20 != 0) {
            throw new LibRuntimeException("'pieces' is not 20 * N!");
        }

        return null;
    };

    private static final Validator<String> NAME = bev -> {
        if (!(bev instanceof ByteStringBeValue value)) {
            throw new LibRuntimeException("'name' or 'path' is not Byte Strings!");
        }

        var bytes = value.getValue();
        if (null == bytes || bytes.length == 0) {
            throw new LibRuntimeException("'name' or 'path' is empty!");
        }

        // 黑名单管理以插件的形式做吧
        // 比如不能包含 . 开头，内部不允许有种子文件， 长度不能超过260 （windows 的限制）
        return new String(bytes, StandardCharsets.UTF_8);
    };

    private static final Validator<Long> LENGTH = bev -> {
        if (!(bev instanceof NumberBeValue value)) {
            throw new LibRuntimeException("'length' is not Number!");
        }

        var length = value.getValue();
        if (length <= 0) {
            throw new LibRuntimeException("'length' must > 0!");
        }
        if (length > ByteUnit.PB.getLength()) {
            throw new LibRuntimeException("'length' illegal!");
        }

        return length;
    };

    private static final Validator<List<String>> PATH = bev -> {
        if (!(bev instanceof ListBeValue value)) {
            throw new LibRuntimeException("'path' is not List!");
        }

        var paths = new ArrayList<String>();
        var iterator = value.iterator();
        while (iterator.hasNext()) {
            var next = iterator.next();
            var path = NAME.validate(next);
            paths.add(path);
        }

        if (paths.isEmpty()) {
            throw new LibRuntimeException("'path' is empty list!");
        }

        return paths;
    };

    private static final Validator<T3tFiles> FILES = bev -> {
        if (!(bev instanceof ListBeValue value)) {
            throw new LibRuntimeException("'files' is not List!");
        }

        var files = new T3tFiles();
        var iterator = value.iterator();
        while (iterator.hasNext()) {
            // 列表中的 File 元素
            var next = iterator.next();
            var file = file(next);
            // 设置文件树的高度
            files.add(file);
        }
        if (files.isEmpty()) {
            throw new LibRuntimeException("'files' is empty!");
        }

        return files;
    };

    private static final Validator<T3tFiles> INFO = bev -> {
        if (!(bev instanceof DictionaryBeValue dict)) {
            throw new LibRuntimeException("'info' is not Dictionary!");
        }

        var length = -1L;
        var flag = 0;
        var directory = false;
        var name = "";

        // 解析文件结构
        T3tFiles files = null;
        var iterator = dict.iterator();
        while (iterator.hasNext()) {
            var item = iterator.next();
            // 判断是否是必须的
            var key = item.getKey();
            var value = item.getValue();
            // 统计出现的所有 key
            if (Objects.equals(Key.PIECE_LENGTH, key)) {
                PIECE_LENGTH.validate(value);
            } else if (Objects.equals(Key.PIECES, key)) {
                PIECES.validate(value);
            } else if (Objects.equals(Key.NAME, key)) {
                name = NAME.validate(value);
            } else {
                if (Objects.equals(Key.LENGTH, key)) {
                    length = LENGTH.validate(value);
                    flag++;
                } else if (Objects.equals(Key.FILES, key)) {
                    files = FILES.validate(value);
                    directory = true;
                    flag++;
                } else {
                    iterator.remove();
                }
            }
        }

        // 判断是否都包含了必须的字段
        var existed = dict.keys().size();
        if (existed != 4 || flag != 1) {
            throw new LibRuntimeException("'info' is not satisfactory!");
        }
        if (Objects.isNull(files)) {
            files = new T3tFiles(new T3tFile(length, name));
        }
        // 文件命名
        files.setDirectory(directory);
        files.setName(name);
        // 构造树形结构
        var root = T3tTree.tree(files);
        files.setRoot(root);

        return files;
    };

    // endregion

}
