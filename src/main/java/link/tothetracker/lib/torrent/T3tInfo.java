package link.tothetracker.lib.torrent;


/**
 * torrent 文件抽象保存的数据结构
 *      单文件也可以认为是 多文件 的一个子集
 *
 * @author t3link
 */
public final record T3tInfo(byte[] bytes,
                            String hash,
                            link.tothetracker.lib.torrent.file.T3tFiles files) {

}
