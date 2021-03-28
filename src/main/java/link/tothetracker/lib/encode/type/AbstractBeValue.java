package link.tothetracker.lib.encode.type;

/**
 * @author t3link
 */
public abstract class AbstractBeValue {

    /**
     * 从源文件拷贝的 b-encode 编码的原始数据
     */
    protected byte[] bytes;

    /**
     * 输出 b-encode 结果
     */
    public byte[] write() {
        // 如果存在拷贝的值 直接用拷贝的
        if (bytes != null) {
            return bytes;
        }

        // 否则 重新编码
        return encode();
    }

    /**
     * 重新 b-encode 编码
     *
     * @return byte[]
     */
    protected abstract byte[] encode();

    public void clone(byte[] bytes) {
        this.bytes = bytes;
    }

}
