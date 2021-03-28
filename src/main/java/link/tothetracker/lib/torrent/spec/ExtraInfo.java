package link.tothetracker.lib.torrent.spec;

import link.tothetracker.lib.encode.type.AbstractBeValue;
import link.tothetracker.lib.encode.type.DictionaryBeValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.collections4.MapUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 为了标识用户的下载行为 追加的一些属性
 * @author t3link
 */
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public final class ExtraInfo extends AbstractBeValue {

    private final String announce;

    private final Map<String, String> extra;

    @Override
    protected byte[] encode() {
        if (MapUtils.isEmpty(this.extra)) {
            return "de".getBytes(StandardCharsets.UTF_8);
        }
        var dict = new DictionaryBeValue();
        extra.forEach((key, value) -> dict.put(Key.build(key), Key.build(value)));
        return dict.write();
    }

}
