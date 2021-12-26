package com.star.easygenerate.setter.service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTypesUtil;
import com.star.easygenerate.setter.model.SetterBO;
import com.star.easygenerate.setter.model.SetterBO.SetterMethodBO;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author wangchao
 * @date 2021/11/21
 */
public class SetterService {

    private static final Map<String, ValueHandler> TYPE_HANDLER_MAPPING = ImmutableMap.<String, ValueHandler>builder()
        .put("boolean", () -> Pair.of(String.valueOf(RandomUtils.nextBoolean()), null))
        .put("java.lang.Boolean", () -> Pair.of(String.valueOf(RandomUtils.nextBoolean()), null))
        .put("int", () -> Pair.of(String.valueOf(RandomUtils.nextInt()), null))
        .put("byte", () -> Pair.of("(byte)" + RandomUtils.nextBytes(1)[0], null))
        .put("java.lang.Byte", () -> Pair.of("(byte)" + RandomUtils.nextBytes(1)[0], null))
        .put("java.lang.Integer", () -> Pair.of(String.valueOf(RandomUtils.nextInt()), null))
        .put("java.lang.String", () -> Pair.of("\"" + RandomStringUtils.randomAlphabetic(8) + "\"", null))
        .put("java.math.BigDecimal", () -> Pair.of("new BigDecimal(\"" + RandomUtils.nextDouble() + "\")", null))
        .put("java.lang.Long", () -> Pair.of(RandomUtils.nextLong() + "L", null))
        .put("long", () -> Pair.of(RandomUtils.nextLong() + "L", null))
        .put("short", () -> Pair.of("(short)" + (short)RandomUtils.nextInt(), null))
        .put("java.lang.Short", () -> Pair.of("(short)" + (short)RandomUtils.nextInt(), null))
        .put("java.util.Date", () -> Pair.of("new Date()", null))
        .put("float", () -> Pair.of(RandomUtils.nextFloat() + "F", null))
        .put("java.lang.Float", () -> Pair.of(RandomUtils.nextFloat() + "F", null))
        .put("double", () -> Pair.of(RandomUtils.nextDouble() + "D", null))
        .put("java.lang.Double", () -> Pair.of(RandomUtils.nextDouble() + "D", null))
        .put("java.lang.Character", () -> Pair.of("'" + RandomStringUtils.randomAlphabetic(1) + "'", null))
        .put("char", () -> Pair.of("'" + RandomStringUtils.randomAlphabetic(1) + "'", null))
        .put("java.time.LocalDateTime", () -> Pair.of("LocalDateTime.now()", null))
        .put("java.time.LocalDate", () -> Pair.of("LocalDate.now()", null))
        .put("java.util.List", () -> Pair.of("new ArrayList<>()", "import java.util.ArrayList;"))
        .put("java.util.Set", () -> Pair.of("new HashSet<>()", "import java.util.HashSet;"))
        .put("java.util.Map", () -> Pair.of("new HashMap<>()", "import java.util.HashMap;"))
        .build();
    private static final ValueHandler DEFAULT_HANDLER = () -> Pair.of("null", null);

    /**
     * 构建setter方法
     *
     * @param psiLocalVariable psi变量
     * @param withDefault 是否有默认值
     * @return {@link SetterBO}
     */
    public SetterBO buildSetters(PsiLocalVariable psiLocalVariable, boolean withDefault) {

        // 准备数据
        PsiClass psiClass = PsiTypesUtil.getPsiClass(psiLocalVariable.getType());

        // 校验
        if (psiClass == null) {
            return null;
        }
        PsiMethod[] psiMethods = psiClass.getAllMethods();
        if (psiMethods.length <= 0) {
            return null;
        }

        List<PsiMethod> setterPsiMethodList = Arrays.stream(psiMethods).filter(m -> {
            // set开头的方法
            if (!m.getName().startsWith("set")) {
                return false;
            }
            // 非static
            if (m.hasModifierProperty(PsiModifier.STATIC)) {
                return false;
            }
            // 非private
            if (m.hasModifierProperty(PsiModifier.PRIVATE)) {
                return false;
            }
            return true;
        }).collect(Collectors.toList());

        SetterBO setter = new SetterBO();
        List<SetterMethodBO> setterMethodList = Lists.newArrayList();
        for (PsiMethod psiMethod : setterPsiMethodList) {
            SetterMethodBO setterMethod = new SetterMethodBO();
            setterMethod.setVariableName(psiLocalVariable.getName());
            setterMethod.setMethodName(psiMethod.getName());
            if (withDefault) {
                List<Pair<String, String>> defaultValueList = getDefaultValue(psiMethod);
                setterMethod.setDefaultValue(defaultValueList.stream().map(Pair::getLeft).collect(Collectors.joining(", ")));
                setterMethod.setImportLines(defaultValueList.stream().map(Pair::getRight).collect(Collectors.toList()));
            }
            setterMethodList.add(setterMethod);
        }
        setter.setSetterMethodList(setterMethodList);
        return setter;
    }

    /**
     * 获取默认值
     *
     * @param psiMethod psi的方法
     * @return {@link String}
     */
    private List<Pair<String, String>> getDefaultValue(PsiMethod psiMethod) {

        PsiParameter[] psiParameters = psiMethod.getParameterList().getParameters();
        List<Pair<String, String>> defaultValues = Lists.newArrayList();
        for (PsiParameter psiParameter : psiParameters) {
            String type = psiParameter.getType().getCanonicalText();
            type = StringUtils.substringBefore(type, "<");

            Pair<String, String> pair = TYPE_HANDLER_MAPPING.getOrDefault(type, DEFAULT_HANDLER).handle();
            defaultValues.add(pair);
        }
        return defaultValues;
    }

    /**
     * handler
     */
    @FunctionalInterface
    private interface ValueHandler {
        /**
         * 处理值
         *
         * @return left:default value, right: java import
         */
        Pair<String, String> handle();
    }
}
