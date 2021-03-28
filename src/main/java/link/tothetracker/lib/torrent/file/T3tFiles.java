package link.tothetracker.lib.torrent.file;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author t3link
 */
@Getter
@Setter
@NoArgsConstructor
public class T3tFiles {

    private List<T3tFile> container = new ArrayList<>();

    /**
     * 是否是文件夹结构
     */
    private boolean directory;

    /**
     * 文件数量
     */
    private int num;

    /**
     * 文件或者文件夹名称
     */
    private String name;

    /**
     * 文件树的根
     */
    private FileNode root;

    /**
     * 单文件构造
     */
    public T3tFiles(T3tFile file) {
        this.container.add(file);
        this.num = 1;
    }

    public void add(T3tFile file) {
        container.add(file);
        num++;
    }

    public boolean isEmpty() {
        return container.isEmpty();
    }

}
