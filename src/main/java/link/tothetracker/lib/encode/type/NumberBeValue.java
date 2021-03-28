package link.tothetracker.lib.encode.type;

import lombok.Getter;

import java.nio.charset.StandardCharsets;

/**
 * @author t3link
 */
public class NumberBeValue extends AbstractBeValue {
	@Getter
	private final long value;

	public NumberBeValue(long value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	protected byte[] encode() {
		return ("i" + value + "e").getBytes(StandardCharsets.UTF_8);
	}
}
