package com.star.easygenerate.setter.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

/**
 * setter
 *
 * @author wangchao
 * @date 2021/11/21
 */
public class SetterBO {

    /** setter方法列表 */
    private List<SetterMethodBO> setterMethodList;

    /**
     * setter方法
     */
    public static class SetterMethodBO {
        /** 变量名 */
        private String variableName;
        /** 方法名称 */
        private String methodName;
        /** 引入类 */
        private List<String> importLines;
        /** 默认值 */
        private String defaultValue;

        public List<String> getImportLines() {
            return importLines;
        }

        public void setImportLines(List<String> importLines) {
            this.importLines = importLines;
        }

        public String getVariableName() {
            return variableName;
        }

        public void setVariableName(String variableName) {
            this.variableName = variableName;
        }

        public String getMethodName() {
            return methodName;
        }

        public void setMethodName(String methodName) {
            this.methodName = methodName;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        /**
         * 获取setter代码
         *
         * @return {@link String}
         */
        public String getSetterCode() {
            if (defaultValue == null) {
                return variableName + "." + methodName + "();";
            } else {
                return variableName + "." + methodName + "(" + defaultValue + ");";
            }
        }

        /**
         * 获取import代码
         *
         * @return {@link String}
         */
        public String getImportCode() {
            if (CollectionUtils.isEmpty(importLines)) {
                return null;
            }
            return importLines.stream().filter(Objects::nonNull).collect(Collectors.joining("\n"));
        }

        @Override
        public String toString() {
            return "SetterMethodBO{" +
                "variableName='" + variableName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", importLines=" + importLines +
                ", defaultValue='" + defaultValue + '\'' +
                '}';
        }
    }

    public List<SetterMethodBO> getSetterMethodList() {
        return setterMethodList;
    }

    public void setSetterMethodList(List<SetterMethodBO> setterMethodList) {
        this.setterMethodList = setterMethodList;
    }

    /**
     * 获取setter代码
     *
     * @return {@link String}
     */
    public String getSetterCode() {
        return setterMethodList.stream().map(SetterMethodBO::getSetterCode).collect(Collectors.joining("\n"));
    }

    /**
     * 获取import代码
     *
     * @return {@link String}
     */
    public String getImportCode() {
        return setterMethodList.stream().map(SetterMethodBO::getImportCode).filter(Objects::nonNull).collect(Collectors.joining("\n"));
    }

    @Override
    public String toString() {
        return "SetterBO{" +
            "setterMethodList=" + setterMethodList +
            '}';
    }
}
