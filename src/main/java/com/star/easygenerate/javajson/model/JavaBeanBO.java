package com.star.easygenerate.javajson.model;

import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

/**
 * java bean类
 *
 * @author wangchao
 * @date 2022/01/02
 */
public class JavaBeanBO {

    /** 序号 */
    private int index;

    /** 类名称 */
    private String className = "Dummy";

    /** import列表 */
    private Set<String> importSet;

    /** 是否是数组类型 */
    private Boolean isArray;

    /** 数组类型 */
    private String arrayType;

    /** 字段列表 */
    private Set<JavaBeanFieldBO> fieldSet;

    /** 内部bean列表 */
    private Set<JavaBeanBO> innerBeanSet;

    @Override
    public String toString() {
        return "JavaBeanBO{" +
            "index=" + index +
            ", className='" + className + '\'' +
            ", importSet=" + importSet +
            ", isArray=" + isArray +
            ", arrayType='" + arrayType + '\'' +
            ", fieldSet=" + fieldSet +
            ", innerBeanSet=" + innerBeanSet +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JavaBeanBO that = (JavaBeanBO)o;
        return Objects.equals(isArray, that.isArray) && Objects.equals(fieldSet, that.fieldSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isArray, fieldSet);
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getArrayType() {
        return arrayType;
    }

    public void setArrayType(String arrayType) {
        this.arrayType = arrayType;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Set<String> getImportSet() {
        return importSet;
    }

    public void setImportSet(Set<String> importSet) {
        this.importSet = importSet;
    }

    public Boolean getIsArray() {
        return isArray;
    }

    public void setIsArray(Boolean isArray) {
        this.isArray = isArray;
    }

    public Set<JavaBeanFieldBO> getFieldSet() {
        return fieldSet;
    }

    public void setFieldSet(Set<JavaBeanFieldBO> fieldSet) {
        this.fieldSet = fieldSet;
    }

    public Set<JavaBeanBO> getInnerBeanSet() {
        return innerBeanSet;
    }

    public void setInnerBeanSet(Set<JavaBeanBO> innerBeanSet) {
        this.innerBeanSet = innerBeanSet;
    }

    /**
     * java bean字段类
     */
    public static class JavaBeanFieldBO implements Comparable<JavaBeanFieldBO> {
        /** 类型 */
        private String type;
        /** 名称 */
        private String name;

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            JavaBeanFieldBO that = (JavaBeanFieldBO)o;
            return Objects.equals(type, that.type) && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, name);
        }

        @Override
        public String toString() {
            return "JavaBeanFieldBO{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
        }

        public JavaBeanFieldBO(String type, String name) {
            this.type = type;
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public int compareTo(@NotNull JavaBeanBO.JavaBeanFieldBO o) {
            return (this.getType() + ":" + this.getName()).compareTo(o.getType() + ":" + o.getName());
        }
    }
}
