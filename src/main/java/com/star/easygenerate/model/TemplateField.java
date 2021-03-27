package com.star.easygenerate.model;

/**
 * @author wangchao
 * @date 2021/03/27
 */
public class TemplateField {

    /** 包名 */
    private String packageName;
    /** 实例名 */
    private String instanceName;
    /** 类名 */
    private String className;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
