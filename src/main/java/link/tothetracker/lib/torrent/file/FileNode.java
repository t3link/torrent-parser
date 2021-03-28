package link.tothetracker.lib.torrent.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import link.tothetracker.lib.LibRuntimeException;
import link.tothetracker.lib.constant.ByteUnit;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author t3link
 */
@Getter
public class FileNode {

    @Setter
    private FileType type;

    private String name;

    private String styledLength;

    /**
     * 子文件
     *
     * linked hash set
     */
    private Set<FileNode> children;

    @JsonIgnore
    @Setter
    private long length;

    @JsonIgnore
    private int depth;

    @JsonIgnore
    private String nodeKey;

    @JsonIgnore
    private FileNode parent;

    @JsonIgnore
    private Set<String> lowerCaseNodeKeys;

    public static FileNode of(String nodeKey, String name, int depth) {
        var node = new FileNode();
        node.nodeKey = nodeKey;
        node.name = name;
        node.depth = depth;
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (FileNode) o;
        return nodeKey.equals(that.nodeKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeKey);
    }

    public long styled(long length) {
        this.length = length;
        this.styledLength = ByteUnit.styled(length);
        return this.length;
    }

    public void add(FileNode node) {
        if (this.children == null) {
            this.children = new LinkedHashSet<>();
            this.lowerCaseNodeKeys = new HashSet<>();
        }

        var result = this.children.add(node);
        if (!result) {
            return;
        }

        // 校验是否有同名的文件 大小写敏感
        if (!lowerCaseNodeKeys.add(node.getNodeKey().toLowerCase())) {
            throw new LibRuntimeException("dupe name '" + node.getName() +
                    "', case-sensitive is not supported now!");
        }

        node.parent = this;
    }

    public boolean extended() {
        return CollectionUtils.isNotEmpty(this.children);
    }

    /**
     * 是否是父亲的最后一个孩子, 打印文件树的时候需要用到
     */
    public boolean isLast() {
        if (null == this.parent) {
            return true;
        }

        var array = this.parent.children.toArray();
        return this.equals(array[array.length - 1]);
    }

}
