package com.star.easygenerate.action;

/**
 * @author wangchao
 * @date 2021/11/18
 */
public class GenerateAllSetter extends BaseGenerateAllSetter {

    @Override
    protected String getTitle() {
        return "Generate all setter";
    }

    @Override
    protected boolean withDefault() {
        return false;
    }
}
