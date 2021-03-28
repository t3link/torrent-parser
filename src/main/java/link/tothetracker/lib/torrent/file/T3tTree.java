package link.tothetracker.lib.torrent.file;


import link.tothetracker.lib.LibRuntimeException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author t3link
 */
public final class T3tTree {

    private T3tTree() {}

    /**
     * 将一个文件的路径描述成树节点
     *
     * 高度：对于任意节点n,n的高度为从n到一片树叶的最长路径长，所有树叶的高度为 0
     * 深度：对于任意节点 n 的深度为从根到 n 的唯一路径长，根的深度为 0；
     * 文件目录根节点的高度, 也就是叶子节点的最大深度
     *
     * 由于这里隐含了一个目录根节点，所以单文件节点的高度为 1
     *
     * 这个字段其实没什么用，只是在创建节点缓冲池的时候预先设置一个合适的大小，提前扩容
     * 但是创建操作里会自动扩容到 paths 大小
     */
    public static FileNode tree(T3tFiles files) {
        // 所有节点组成的节点池, 防止重复创建节点对象
        var pools = new ArrayList<LinkedHashMap<String, FileNode>>();

        // 是否是文件夹结构
        var directory = files.isDirectory();

        for (var file : files.getContainer()) {
            build(file, pools);
        }

        // 找到深度为 1 的根节点
        var pool0 = pools.get(0);
        if (null == pool0 || pool0.size() == 0) {
            throw new LibRuntimeException("root file node not found!");
        }

        if (!directory && pool0.size() > 1) {
            throw new LibRuntimeException("single file only accept 1 root file node!");
        }

        // 封装一个虚拟的根节点, 如果为单文件, 根节点名称是 '.' , 如果是多文件, 节点名称是文件夹名称
        var rootName = directory ? files.getName() : ".";
        var virtualNode = FileNode.of("", rootName, 0);
        for (var root : pool0.values()) {
            virtualNode.add(root);
        }
        // 计算长度
        calc(virtualNode);
        return virtualNode;
    }

    /**
     * 将一个文件的路径描述成树节点
     */
    private static void build(T3tFile file, List<LinkedHashMap<String, FileNode>> pools) {
        // 从叶子节点 -> 根节点
        FileNode child = null;
        var paths = file.getPath();
        for (var index = paths.size() - 1;  index >= 0; index --) {
            var node = pooled(paths, index + 1, pools);
            // 指定叶子节点
            if (index == paths.size() - 1) {
                node.setLength(file.getLength());
            }
            // 子节点关联父节点
            if (child != null) {
                node.add(child);
            }
            // 作为上一级的子节点
            child = node;
        }
    }

    /**
     * 从节点池中检查取出(创建)节点
     * 如果当前高度的节点在池中存在, 则直接取出
     * @param paths             文件名字, 对应文件夹或者文件名称
     * @param depth             节点深度
     * @param pools             节点池
     */
    private static FileNode pooled(List<String> paths, int depth,
                                   List<LinkedHashMap<String, FileNode>> pools) {
        var name = paths.get(depth - 1);
        var key = identify(paths, depth);
        // 如果池为空 则直接创建
        if (null == pools) {
            return FileNode.of(key, name, depth);
        }

        // 判断池中是否有当前高度的节点, 如果小于当前高度, 需要增长到此高度
        while (depth > pools.size()) {
            pools.add(new LinkedHashMap<>());
        }

        var pool1 = pools.get(depth - 1);
        // 创建完的 非叶子节点 需要放入池中
        return pool1.computeIfAbsent(
                key, e -> FileNode.of(e, name, depth)
        );
    }

    /**
     * 高度和文件夹名称不能决定一个文件夹, 因为可能存在同高度的同名文件夹
     * @param paths     一个文件路径包含的列表
     * @param depth     文件/文件夹在的深度
     */
    private static String identify(List<String> paths, int depth) {
        var fullPathList = paths.subList(0, depth);
        var key = new StringBuilder();
        for (var idx = 0; idx < depth; idx ++) {
            var path = fullPathList.get(idx);
            // 生成 b-encode 编码来确定一个路径
            key.append(path.length()).append(':').append(path);
            if (idx != depth - 1) {
                key.append(',');
            }
        }

        return key.toString();
    }

    /**
     * 递归格式化节点的大小
     */
    private static long calc(FileNode node) {
        FileType.init(node);
        var children = node.getChildren();
        if (null == children) {
            return node.styled(node.getLength());
        }
        var length = 0L;
        for (var child : children) {
            length += calc(child);
        }

        return node.styled(length);
    }

    /**
     * 打印文件树
     * .
     * └── hello
     */
    public static String print(FileNode node) {
        var tree = new StringBuilder();
        if (node.getDepth() == 0) {
            tree.append(node.getName());
            tree.append(" ");
            tree.append(node.getStyledLength());
            tree.append("\n");
        } else {
            tree.append(padding(node));
            tree.append(node.getName());
            tree.append(" ");
            tree.append(node.getStyledLength());
            tree.append("\n");
        }

        if (node.getChildren() == null) {
            return tree.toString();
        }
        for (var child : node.getChildren()) {
            tree.append(print(child));
        }

        return tree.toString();
    }

    /**
     * 占位符美化
     */
    private static String padding(FileNode node) {
        var builder = new StringBuilder();
        var depth = node.getDepth();
        var brothers = new HashSet<Integer>();
        // 当他的父亲有兄弟的时候 | 连接符
        var parent = node.getParent();
        while (parent != null) {
            var grand = parent.getParent();
            if (grand != null && grand.extended() && !parent.isLast()) {
                brothers.add(parent.getDepth());
            }
            parent = parent.getParent();
        }

        for(var i = 1; i < depth; ++i) {
            if (brothers.contains(i)) {
                builder.append("|   ");
            } else {
                builder.append("    ");
            }
        }

        builder.append("|--- ");
        return builder.toString();
    }

}
