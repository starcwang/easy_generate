package com.star.easygenerate.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author wangchao
 * @date 2020/12/13
 */
public final class WordUtil {

    private WordUtil() {}

    /**
     * 驼峰表示法变成下划线大写表示法
     *
     * @param camelCase 驼峰式
     * @return {@link String}
     */
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
