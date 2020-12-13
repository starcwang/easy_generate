package com.star.easygenerate.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author wangchao
 * @date 2020/12/13
 */
public final class WordUtils {

    private WordUtils() {}

    public static String toUpperCase(String camelCase) {
        if (StringUtils.isBlank(camelCase)) {
            return camelCase;
        }
        camelCase = camelCase.replaceAll("(?<=[^A-Z])[A-Z][^A-Z]", "_$0");
        camelCase = camelCase.replaceAll("[A-Z]{2,}", "_$0");
        camelCase = camelCase.replaceAll("_+", "_");
        return camelCase.toUpperCase();
    }
}
