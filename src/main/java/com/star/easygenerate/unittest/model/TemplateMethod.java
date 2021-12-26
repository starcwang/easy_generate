package com.star.easygenerate.unittest.model;

/**
 * @author wangchao
 * @date 2021/03/27
 */
public class TemplateMethod {

    /** private,public */
    private String scope;
    /** 方法名称 */
    private String name;
    /** 方法名称(首字母大写) */
    private String nameUp;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameUp() {
        return nameUp;
    }

    public void setNameUp(String nameUp) {
        this.nameUp = nameUp;
    }
}
