package link.tothetracker.lib.torrent.spec;

import link.tothetracker.lib.encode.type.AbstractBeValue;

/**
 * @author t3link
 */
@FunctionalInterface
interface Validator<T> {

    /**
     * 校验并返回所需要的结果
     *
     * @param value 原始信息
     * @return 如果校验通过返回结果，否则抛出异常
     */
    T validate(AbstractBeValue value) ;

}
