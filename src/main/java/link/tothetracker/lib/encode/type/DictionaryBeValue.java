package link.tothetracker.lib.encode.type;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author t3link
 */
public class DictionaryBeValue extends AbstractBeValue {
	private final SortedMap<ByteStringBeValue, AbstractBeValue> value;

	public DictionaryBeValue(SortedMap<ByteStringBeValue, AbstractBeValue> value) {
		this.value = value;
	}
	public DictionaryBeValue() {
		this.value = new TreeMap<>();
	}

	public AbstractBeValue get(ByteStringBeValue key) {
		return this.value.get(key);
	}

	public Iterator<Map.Entry<ByteStringBeValue, AbstractBeValue>> iterator() {
		return this.value.entrySet().iterator();
	}

	public Set<ByteStringBeValue> keys() {
		return this.value.keySet();
	}

	public void put(ByteStringBeValue key, AbstractBeValue value) {
		// 确保文件的缓存 bytes 被清除
		this.value.put(key, value);
	}

	@Override
	protected byte[] encode() {
		var output = ArrayUtils.addAll("d".getBytes(StandardCharsets.UTF_8));
		for (var entry : this.value.entrySet()) {
			var key = entry.getKey();
			byte[] bytes = key.write();
			output = ArrayUtils.addAll(output, bytes);

			var entryValue = entry.getValue();
			bytes = entryValue.write();
			output = ArrayUtils.addAll(output, bytes);
		}
		return ArrayUtils.addAll(output, "e".getBytes(StandardCharsets.UTF_8));
	}
}
