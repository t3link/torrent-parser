package link.tothetracker.lib.encode;

import link.tothetracker.lib.LibRuntimeException;
import link.tothetracker.lib.encode.type.*;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.TreeMap;

/**
 * @author t3link
 */
public class BeDecoder {
    private final InputStream in;

    // The last indicator read.
    // Zero if unknown.
    // '0'..'9' indicates a byte[].
    // 'i' indicates an Number.
    // 'l' indicates a List.
    // 'd' indicates a Map.
    // 'e' indicates end of Number, List or Map (only used internally).
    // -1 indicates end of stream.
    // Call getNextIndicator to get the current value (will never return zero).

    private static final char FLAG_NUMBER = 'i';
    private static final char FLAG_LIST = 'l';
    private static final char FLAG_DICT = 'd';
    private static final char FLAG_SPLIT = ':';
    private static final char FLAG_END = 'e';

    private static final char FLAG_NUMBER_NEGATIVE = '-';
    private static final char FLAG_NUMBER_0 = '0';
    private static final char FLAG_NUMBER_1 = '1';
    private static final char FLAG_NUMBER_9 = '9';
    private static final int NUMBER_0 = 0;
    private static final int NUMBER_9 = 9;

    private int indicator = 0;

    public BeDecoder(InputStream in) {
        this.in = in;
    }

    public static AbstractBeValue decode(InputStream in) throws IOException {
        return new BeDecoder(in).decode();
    }

    public static AbstractBeValue decode(byte[] data) throws IOException {
        return BeDecoder.decode(new ByteArrayInputStream(data));
    }

    /**
     * Gets the next indicator and returns either null when the stream
     * has ended or b-decodes the rest of the stream and returns the
     * appropriate BEValue encoded object.
     */
    private AbstractBeValue decode() throws IOException {
        var begin = this.reacquireIndicator(false);
        if (begin == -1) {
            throw new LibRuntimeException("stream read: -1");
        }

        if (begin >= FLAG_NUMBER_0 && begin <= FLAG_NUMBER_9) {
            return this.decodeBytes();
        } else if (begin == FLAG_NUMBER) {
            return this.decodeNumber();
        } else if (begin == FLAG_LIST) {
            return this.decodeList();
        } else if (begin == FLAG_DICT) {
            return this.decodeDictionary();
        } else {
            throw new LibRuntimeException("unknown indicator!");
        }
    }

    /**
     * Returns the next b-encoded value on the stream and makes sure it is a byte array.
     *
     * @throws IOException If it is not a b-encoded byte array.
     */
    public AbstractBeValue decodeBytes() throws IOException {
        var begin = this.reacquireIndicator(true);
        var num = begin - FLAG_NUMBER_0;
        if (num < NUMBER_0 || num > NUMBER_9) {
            throw new LibRuntimeException("number expected!");
        }

        var next = this.read();
        var i = next - FLAG_NUMBER_0;
        while (i >= NUMBER_0 && i <= NUMBER_9) {
            // This can overflow!
            num = num * 10 + i;
            next = this.read();
            i = next - FLAG_NUMBER_0;
        }

        if (next != FLAG_SPLIT) {
            throw  new LibRuntimeException("colon expected!");
        }

        return new ByteStringBeValue(read(num));
    }

    /**
     * Returns the next b-encoded value on the stream and makes sure it is a number.
     *
     * @throws IOException If it is not a number.
     */
    public AbstractBeValue decodeNumber() throws IOException {
        var begin = this.reacquireIndicator(true);
        if (begin != FLAG_NUMBER) {
            throw new LibRuntimeException("'i' expected!");
        }

        var next = this.read();
        if (next == FLAG_NUMBER_0) {
            next = this.read();
            if (next == FLAG_END) {
                return new NumberBeValue(0L);
            }

            throw new LibRuntimeException("'e' after zero expected!");
        }

        var isNegative = false;

        if (next == FLAG_NUMBER_NEGATIVE) {
            next = this.read();
            if (next == FLAG_NUMBER_0) {

                throw new LibRuntimeException("'-0' not allowed!");
            }
            isNegative = true;
        }

        if (next < FLAG_NUMBER_1 || next > FLAG_NUMBER_9) {
            throw new LibRuntimeException("invalid number!");
        }

        var number = (long) (next - FLAG_NUMBER_0);
        next = this.read();
        var i = next - FLAG_NUMBER_0;
        while (i >= NUMBER_0 && i <= NUMBER_9) {
            number = number * 10L + i;
            next = this.read();
            i = next - FLAG_NUMBER_0;
        }

        if (next != FLAG_END) {
            throw new LibRuntimeException("'e' expected end!");
        }

        var value = isNegative ? (- number) : number;
        return new NumberBeValue(value);
    }

    /**
     * Returns the next b-encoded value on the stream and makes sure it is a list.
     *
     * @throws IOException If it is not a list.
     */
    public AbstractBeValue decodeList() throws IOException {
        var begin = this.reacquireIndicator(true);
        if (begin != FLAG_LIST) {
            throw new LibRuntimeException("'l' expected!");
        }
        var result = new ArrayList<AbstractBeValue>();
        var next = this.reacquireIndicator(false);
        while (next != FLAG_END) {
            var value = this.decode();
            result.add(value);
            next = this.reacquireIndicator(false);
        }

        this.indicator = 0;

        return new ListBeValue(result);
    }

    /**
     * Returns the next b-encoded value on the stream and makes sure it is a
     * map (dictionary).
     *
     * @throws IOException If it is not a map.
     */
    public AbstractBeValue decodeDictionary() throws IOException {
        var begin = this.reacquireIndicator(true);
        if (begin != FLAG_DICT) {
            throw new LibRuntimeException("'d' expected!");
        }

        var dictionary = new TreeMap<ByteStringBeValue, AbstractBeValue>();
        var next = this.reacquireIndicator(false);
        while (next != FLAG_END) {
            // Dictionary keys are always strings.
            var key = this.decode();
            if (!(key instanceof ByteStringBeValue)) {
                throw new LibRuntimeException("invalid key in dictionary!");
            }

            var value = this.decode();
            dictionary.put((ByteStringBeValue) key, value);

            next = this.reacquireIndicator(false);
        }

        this.indicator = 0;

        return new DictionaryBeValue(dictionary);
    }

    /**
     * Returns what the next b-encoded object will be on the stream or -1
     * when the end of stream has been reached.
     *
     * <p>
     * Can return something unexpected (not '0' .. '9', 'i', 'l' or 'd') when
     * the stream isn't b-encoded.
     * </p>
     *
     * This might or might not read one extra byte from the stream.
     */
    private int reacquireIndicator(boolean reset) throws IOException {
        if (this.indicator == 0) {
            this.indicator = this.in.read();
        }

        var currentIndicator =  this.indicator;
        if (reset) {
            this.indicator = 0;
        }
        return currentIndicator;
    }


    /**
     * Returns the next byte read from the InputStream (as int).
     *
     * @throws EOFException If InputStream.read() returned -1.
     */
    private int read() throws IOException {
        var c = this.in.read();
        if (c == -1) {
            throw new EOFException();
        }
        return c;
    }

    /**
     * Returns a byte[] containing length valid bytes starting at offset zero.
     *
     * @throws EOFException If InputStream.read() returned -1 before all
     * requested bytes could be read.  Note that the byte[] returned might be
     * bigger then requested but will only contain length valid bytes. The
     * returned byte[] will be reused when this method is called again.
     */
    private byte[] read(int length) throws IOException {
        var result = new byte[length];

        var read = 0;
        while (read < length) {
            var i = this.in.read(result, read, length - read);
            if (i == -1) {
                throw new EOFException();
            }
            read += i;
        }

        return result;
    }
}
