package com.star.easygenerate.unittest.model;

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
    /** java文档 */
    private String javaDoc;

    public String getJavaDoc() {
        return javaDoc;
    }

    public void setJavaDoc(String javaDoc) {
        this.javaDoc = javaDoc;
    }

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
