package link.tothetracker.lib.torrent.file;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件类型
 * @author t3link
 */
@Getter
@AllArgsConstructor
public enum FileType {

	/**
	 * 文件夹
	 */
	FOLDER,

	/**
	 * 文件
	 */
	FILE,

	/**
	 * 文本文件
	 */
	TEXT,

	/**
	 * ISO
	 */
	ISO,

	/**
	 * 视频
	 */
	VIDEO,

	/**
	 * 音频
	 */
	AUDIO,

	/**
	 * 图片
	 */
	IMG,

	/**
	 * 压缩包
	 */
	ARCHIVE,

	;

	private static final Map<String, FileType> FILE_TYPE_MAPPER;

	static {
		var builder = new HashMap<String, FileType>(16);
		builder.put("mkv", FileType.VIDEO);
		builder.put("mp4", FileType.VIDEO);
		builder.put("m2ts", FileType.VIDEO);
		builder.put("ts", FileType.VIDEO);
		builder.put("tp", FileType.VIDEO);
		builder.put("avi", FileType.VIDEO);
		builder.put("flv", FileType.VIDEO);
		builder.put("rmvb", FileType.VIDEO);

		builder.put("flac", FileType.AUDIO);
		builder.put("m4a", FileType.AUDIO);
		builder.put("aac", FileType.AUDIO);
		builder.put("wav", FileType.AUDIO);
		builder.put("ac3", FileType.AUDIO);
		builder.put("mp3", FileType.AUDIO);

		builder.put("png", FileType.IMG);
		builder.put("bmp", FileType.IMG);
		builder.put("webp", FileType.IMG);
		builder.put("jpg", FileType.IMG);
		builder.put("jpeg", FileType.IMG);
		builder.put("gif", FileType.IMG);

		builder.put("nfo", FileType.TEXT);
		builder.put("md", FileType.TEXT);
		builder.put("txt", FileType.TEXT);
		builder.put("ssa", FileType.TEXT);
		builder.put("ass", FileType.TEXT);
		builder.put("srt", FileType.TEXT);
		builder.put("log", FileType.TEXT);
		builder.put("cue", FileType.TEXT);
		builder.put("sup", FileType.TEXT);

		builder.put("iso", FileType.ISO);

		builder.put("zip", FileType.ARCHIVE);
		builder.put("rar", FileType.ARCHIVE);
		builder.put("7z", FileType.ARCHIVE);
		builder.put("tar", FileType.ARCHIVE);
		builder.put("gz", FileType.ARCHIVE);

		FILE_TYPE_MAPPER = Map.copyOf(builder);
	}

	/**
	 * 判断文件类型
	 */
	private static FileType init0(FileNode node) {
		if (node.getChildren() != null) {
			return FileType.FOLDER;
		}

		var name = node.getName();

		var idx = name.lastIndexOf(".");
		if (idx <= 0) {
			return FileType.FILE;
		}

		var type = name.substring(idx + 1).toLowerCase();
		return FILE_TYPE_MAPPER.getOrDefault(type, FileType.FILE);
	}

	public static void init(FileNode node) {
		var type = init0(node);
		node.setType(type);
	}


}
