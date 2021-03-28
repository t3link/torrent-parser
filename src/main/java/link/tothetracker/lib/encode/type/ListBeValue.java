package link.tothetracker.lib.encode.type;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

/**
 * @author t3link
 */
public class ListBeValue extends AbstractBeValue {
	private final List<AbstractBeValue> value;

	public ListBeValue(List<AbstractBeValue> value) {
		this.value = value;
	}

	public Iterator<AbstractBeValue> iterator() {
		return this.value.iterator();
	}

	@Override
	protected byte[] encode() {
		var output = ArrayUtils.addAll("l".getBytes(StandardCharsets.UTF_8));
		for (var beValue : value) {
			var bytes = beValue.write();
			output = ArrayUtils.addAll(output, bytes);
		}

		return ArrayUtils.addAll(output, "e".getBytes(StandardCharsets.UTF_8));
	}
}
