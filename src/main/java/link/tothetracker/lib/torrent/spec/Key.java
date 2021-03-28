package link.tothetracker.lib.torrent.spec;

import link.tothetracker.lib.encode.type.ByteStringBeValue;

/**
 * https://wiki.theory.org/index.php/BitTorrentSpecification#Metainfo_File_Structure
 * @author t3link
 */
interface Key {

    /**
     * a dictionary that describes the file(s) of the torrent
     */
    ByteStringBeValue INFO = build("info");

    /**
     * The announce URL of the tracker (string)
     */
    ByteStringBeValue ANNOUNCE = build("announce");

    /**
     * (optional) this is an extension to the official specification, offering
     * backwards-compatibility. (list of lists of strings).
     * The official request for a specification change is
     */
    ByteStringBeValue ANNOUNCE_LIST = build("announce-list");

    /**
     * (optional) the string encoding format used to generate the pieces part of the info
     * dictionary in the .torrent metafile (string)
     */
    ByteStringBeValue ENCODING = build("encoding");

    /**
     * (optional) name and version of the program used to create the .torrent (string)
     */
    ByteStringBeValue CREATION = build("creation");

    /**
     * (optional) free-form textual comments of the author (string)
     */
    ByteStringBeValue COMMENT = build("comment");

    /**
     * (optional) name and version of the program used to create the .torrent (string)
     */
    ByteStringBeValue CREATED_BY = build("created by");

    /**
     * number of bytes in each piece (integer)
     */
    ByteStringBeValue PIECE_LENGTH = build("piece length");

    /**
     * string consisting of the concatenation of all 20-byte SHA1 hash values, one per piece
     * (byte string, i.e. not urlencoded)
     */
    ByteStringBeValue PIECES = build("pieces");

    ByteStringBeValue SOURCE = build("source");

    /**
     * (optional) this field is an integer. If it is set to "1", the client MUST publish its
     * presence to get other peers ONLY via the trackers explicitly described in the metainfo file.
     * If this field is set to "0" or is not present, the client may obtain peer from other means,
     * e.g. PEX peer exchange, dht. Here, "private" may be read as "no external peer identify".
     */
    ByteStringBeValue PRIVATE = build("private");

    /**
     * the filename. This is purely advisory. (string)
     */
    ByteStringBeValue NAME = build("name");

    ByteStringBeValue NAME_UTF8 = build("name.utf-8");

    /**
     * length of the file in bytes (integer)
     */
    ByteStringBeValue LENGTH = build("length");

    /**
     * a list of dictionaries, one for each file. Each dictionary in this list contains
     * the following keys
     */
    ByteStringBeValue FILES = build("files");

    /**
     * a list containing one or more string elements that together represent the path and filename.
     * Each element in the list corresponds to either a directory name
     * or (in the case of the final element) the filename.
     * For example, a the file "dir1/dir2/file.ext" would
     * consist of three string elements: "dir1", "dir2", and "file.ext".
     * This is encoded as a bencoded list of strings such as l4:dir14:dir28:file.exte
     */
    ByteStringBeValue PATH = build("path");

    ByteStringBeValue PATH_UTF8 = build("path.utf-8");

    /**
     * (optional) a 32-character hexadecimal string corresponding to the MD5 sum of the file.
     * This is not used by BitTorrent at all,
     * but it is included by some programs for greater compatibility.
     */
    ByteStringBeValue MD5_SUM = build("md5sum");


    //////// 以下是自定义的一些 key

    /**
     * 站点信息
     */
    ByteStringBeValue SITE_INFO = build("site info");

    /**
     * 构造函数
     *
     * @param key 字段名
     * @return ByteStringValue
     */
    static ByteStringBeValue build(String key) {
        return new ByteStringBeValue(key);
    }

}
