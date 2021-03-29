package link.tothetracker.lib.torrent.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import link.tothetracker.lib.LibRuntimeException;
import link.tothetracker.lib.constant.ByteUnit;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author t3link
 */
@Getter
@JsonPropertyOrder(value = {"id", "type", "name", "size", "children"})
public class FileNode {

    @Setter
    private int id;

    @Setter
    private FileType type;

    @Setter
    private String name;

    @Setter
    private String size;

    /**
     * 子文件
     *
     * linked hash set
     */
    @Setter
    private List<FileNode> children;

    @JsonIgnore
    @Setter
    private long length;

    @JsonIgnore
    private int depth;

    @JsonIgnore
    private FileNode parent;

    @JsonIgnore
    private String nodeKey;

    /**
     * 判断是否之前添加过这个 key
     * 忽略大小写
     */
    @JsonIgnore
    private Set<String> insensitiveKeys;

    public static FileNode of(IdGenerator gen, String nodeKey, String name, int depth) {
        var node = new FileNode();
        if (gen != null) {
            node.id = gen.add();
        }
        node.nodeKey = nodeKey;
        node.name = name;
        node.depth = depth;
        return node;
    }

    public long styled(long length) {
        this.length = length;
        this.size = ByteUnit.styled(length);
        return this.length;
    }

    @JsonIgnore
    public boolean extended() {
        return CollectionUtils.isNotEmpty(this.children);
    }

    /**
     * 是否是父亲的最后一个孩子, 打印文件树的时候需要用到
     */
    @JsonIgnore
    public boolean last() {
        if (null == this.parent) {
            return true;
        }

        var array = this.parent.children.toArray();
        return this.equals(array[array.length - 1]);
    }

    public void add(FileNode node) {
        if (this.children == null) {
            this.children = new ArrayList<>();
            this.insensitiveKeys = new HashSet<>();
        }

        // 说明之前已经添加过了
        if (node.parent != null) {
            if (!node.parent.equals(this)) {
                throw new LibRuntimeException("conflict parents, " + node.name);
            }

            return;
        }

        var lowerKey = node.nodeKey.toLowerCase();
        if (!insensitiveKeys.add(lowerKey)) {
            throw new LibRuntimeException("dupe name '" +
                    node.name + "', case-sensitive is not supported now!");
        }

        this.children.add(node);
        node.parent = this;
    }

}
