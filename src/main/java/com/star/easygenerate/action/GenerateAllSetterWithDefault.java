package com.star.easygenerate.action;

/**
 * @author wangchao
 * @date 2021/11/18
 */
public class GenerateAllSetterWithDefault extends BaseGenerateAllSetter {

    @Override
    protected String getTitle() {
        return "Generate all setter with default";
    }

    @Override
    protected boolean withDefault() {
        return true;
    }
}
