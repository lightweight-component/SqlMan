package com.ajaxjs.util.enums;

import com.ajaxjs.util.StrUtil;

import java.util.Optional;

public class EnumsUtil {
    public static <E, V, T extends IEnum<E, V>> Optional<T> of(E code, Class<T> cla) {
        T[] enums = cla.getEnumConstants();

        for (T value : enums) {
            if (code == value.getCode())
                return Optional.of(value);
        }

        return Optional.empty();
    }


    public static <E, V, T extends IEnum<E, V>> V ofMsg(E code, Class<T> cla) {
        Optional<T> of = of(code, cla);

        return of.map(IEnum::getMsg).orElse(null);
    }

    public static <E, V, T extends IEnum<E, V>> E ofCode(String msg, Class<T> cla) {
        if (StrUtil.isEmptyText(msg))
            return null;

        T[] enums = cla.getEnumConstants();

        for (T value : enums) {
            if (msg.equals(value.getMsg()))
                return value.getCode();
        }

        return null;
    }
}
