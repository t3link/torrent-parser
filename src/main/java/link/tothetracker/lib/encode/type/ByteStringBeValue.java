package link.tothetracker.lib.encode.type;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author t3link
 */
public class ByteStringBeValue extends AbstractBeValue implements Comparable<ByteStringBeValue> {

	/**
	 * byte string 的值 [其实也可以直接用 String 类型]
	 */
	@Getter
	private final byte[] value;

	public ByteStringBeValue(byte[] value) {
		this.value = value;
	}

	public ByteStringBeValue(String value) {
		this.value = value.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || this.getClass() != o.getClass()) {
			return false;
		}
		ByteStringBeValue that = (ByteStringBeValue) o;
		return Arrays.equals(value, that.value);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(value);
	}

	@Override
	protected byte[] encode() {
		byte[] b1 = (value.length + ":").getBytes(StandardCharsets.UTF_8);
		return ArrayUtils.addAll(b1, value);
	}

	@Override
	public String toString() {
		return new String(this.value, StandardCharsets.UTF_8);
	}

	@Override
	public int compareTo(ByteStringBeValue other) {
		return this.toString().compareTo(other.toString());
	}

}
