package link.tothetracker.lib.torrent.file;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author t3link
 */
@Getter
@Setter
public class T3tFile {

    private long length;

    /**
     *  a list containing one or more string elements that together represent the path and filename.
     *  Each element in the list corresponds to either a directory name
     *  or (in the case of the final element) the filename.
     *  For example, a the file "dir1/dir2/file.ext" would consist of three string elements:
     *  "dir1", "dir2", and "file.ext".
     *  This is encoded as a bencoded list of strings such as l4:dir14:dir28:file.exte
     */
    private List<String> path;

    public T3tFile(long length, List<String> path) {
        this.length = length;
        this.path = path;
    }
    public T3tFile(long length, String path) {
        this.length = length;
        this.path = List.of(path);
    }

}
