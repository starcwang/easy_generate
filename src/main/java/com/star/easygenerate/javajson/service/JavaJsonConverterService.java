package com.star.easygenerate.javajson.service;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.function.Supplier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.ShortNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.history.VcsRevisionNumber.Int;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.util.ResourceUtil;
import com.star.easygenerate.javajson.model.JavaBeanBO;
import com.star.easygenerate.javajson.model.JavaBeanBO.JavaBeanFieldBO;
import com.star.easygenerate.util.DateUtil;
import com.star.easygenerate.util.JsonUtil;
import com.star.easygenerate.util.RandomUtil;
import com.star.easygenerate.util.StringUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

/**
 * @author wangchao
 * @date 2022/01/01
 */
public class JavaJsonConverterService {

    /** java import template */
    private static final String IMPORT_TEMPLATE = "import %s;";

    /** type converter */
    private static final Map<String, Supplier<?>> TYPE_VALUE_MAP = ImmutableMap.<String, Supplier<?>>builder()
        .put(byte.class.getCanonicalName(), () -> RandomUtils.nextBytes(1)[0])
        .put(short.class.getCanonicalName(), () -> (short)RandomUtils.nextInt())
        .put(int.class.getCanonicalName(), RandomUtil::nextInt)
        .put(long.class.getCanonicalName(), RandomUtil::nextLong)
        .put(float.class.getCanonicalName(), RandomUtil::nextFloat)
        .put(double.class.getCanonicalName(), () -> RandomUtil.nextDoubleInThousand(2))
        .put(char.class.getCanonicalName(), () -> RandomStringUtils.randomAlphabetic(1).charAt(0))
        .put(boolean.class.getCanonicalName(), RandomUtil::nextBoolean)
        .put(Byte.class.getCanonicalName(), () -> RandomUtils.nextBytes(1)[0])
        .put(Short.class.getCanonicalName(), () -> (short)RandomUtils.nextInt())
        .put(Integer.class.getCanonicalName(), RandomUtil::nextInt)
        .put(Long.class.getCanonicalName(), RandomUtil::nextLong)
        .put(Float.class.getCanonicalName(), RandomUtil::nextFloat)
        .put(Double.class.getCanonicalName(), () -> RandomUtil.nextDoubleInThousand(2))
        .put(Character.class.getCanonicalName(), () -> RandomStringUtils.randomAlphabetic(1).charAt(0))
        .put(Boolean.class.getCanonicalName(), RandomUtil::nextBoolean)
        .put(String.class.getCanonicalName(), () -> RandomStringUtils.randomAlphanumeric(6))
        .put(BigDecimal.class.getCanonicalName(), () -> BigDecimal.valueOf(RandomUtil.nextDoubleInThousand(2)))
        .put(Date.class.getCanonicalName(), () -> LocalDateTime.now().format(DateUtil.DATE_TIME_FORMATTER))
        .put(LocalDate.class.getCanonicalName(), () -> LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
        .put(LocalDateTime.class.getCanonicalName(), () -> LocalDateTime.now().format(DateUtil.DATE_TIME_FORMATTER))

        .put(byte[].class.getCanonicalName(), () -> RandomUtils.nextBytes(2))
        .put(short[].class.getCanonicalName(), () -> new short[] {(short)RandomUtils.nextInt(), (short)RandomUtils.nextInt()})
        .put(int[].class.getCanonicalName(), () -> new int[] {RandomUtils.nextInt(), RandomUtils.nextInt()})
        .put(long[].class.getCanonicalName(), () -> new long[] {RandomUtils.nextLong(), RandomUtils.nextLong()})
        .put(float[].class.getCanonicalName(), () -> new float[] {RandomUtils.nextFloat(), RandomUtils.nextFloat()})
        .put(double[].class.getCanonicalName(), () -> new double[] {RandomUtil.nextDoubleInThousand(2), RandomUtil.nextDoubleInThousand(2)})
        .put(char[].class.getCanonicalName(), () -> RandomStringUtils.randomAlphabetic(2).toCharArray())
        .put(boolean[].class.getCanonicalName(), () -> new boolean[] {RandomUtils.nextBoolean(), RandomUtils.nextBoolean()})
        .put(Byte[].class.getCanonicalName(), () -> RandomUtils.nextBytes(2))
        .put(Short[].class.getCanonicalName(), () -> new short[] {(short)RandomUtils.nextInt(), (short)RandomUtils.nextInt()})
        .put(Int[].class.getCanonicalName(), () -> new int[] {RandomUtils.nextInt(), RandomUtils.nextInt()})
        .put(Long[].class.getCanonicalName(), () -> new long[] {RandomUtils.nextLong(), RandomUtils.nextLong()})
        .put(Float[].class.getCanonicalName(), () -> new float[] {RandomUtils.nextFloat(), RandomUtils.nextFloat()})
        .put(Double[].class.getCanonicalName(), () -> new double[] {RandomUtil.nextDoubleInThousand(2), RandomUtil.nextDoubleInThousand(2)})
        .put(Character[].class.getCanonicalName(), () -> RandomStringUtils.randomAlphabetic(2).toCharArray())
        .put(Boolean[].class.getCanonicalName(), () -> new boolean[] {RandomUtils.nextBoolean(), RandomUtils.nextBoolean()})
        .put(String[].class.getCanonicalName(), () -> new String[] {RandomStringUtils.randomAlphanumeric(6),
            RandomStringUtils.randomAlphanumeric(6)})
        .put(BigDecimal[].class.getCanonicalName(), () -> new BigDecimal[] {BigDecimal.valueOf(RandomUtil.nextDoubleInThousand(2)),
            BigDecimal.valueOf(RandomUtil.nextDoubleInThousand(2))})
        .put(Date[].class.getCanonicalName(), () -> new String[] {LocalDateTime.now().format(DateUtil.DATE_TIME_FORMATTER),
            LocalDateTime.now().format(DateUtil.DATE_TIME_FORMATTER)})
        .put(LocalDate[].class.getCanonicalName(), () -> new String[] {LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)})
        .put(LocalDateTime[].class.getCanonicalName(), () -> new String[] {LocalDateTime.now().format(DateUtil.DATE_TIME_FORMATTER),
            LocalDateTime.now().format(DateUtil.DATE_TIME_FORMATTER)})
        .build();

    /** collections */
    private static final List<String> COLLECTION_LIST = ImmutableList.<String>builder()
        .add(Collection.class.getCanonicalName())
        .add(List.class.getCanonicalName())
        .add(Set.class.getCanonicalName())
        .add(SortedSet.class.getCanonicalName())
        .add(NavigableSet.class.getCanonicalName())
        .add(Deque.class.getCanonicalName())
        .add(Queue.class.getCanonicalName())
        .add(AbstractCollection.class.getCanonicalName())
        .add(AbstractList.class.getCanonicalName())
        .add(AbstractSet.class.getCanonicalName())
        .add(AbstractSequentialList.class.getCanonicalName())
        .add(HashSet.class.getCanonicalName())
        .add(TreeSet.class.getCanonicalName())
        .add(ArrayList.class.getCanonicalName())
        .add(LinkedList.class.getCanonicalName())
        .add(Vector.class.getCanonicalName())
        .add(Stack.class.getCanonicalName())
        .add(LinkedHashSet.class.getCanonicalName())
        .build();

    /** maps */
    private static final List<String> MAP_LIST = ImmutableList.<String>builder()
        .add(Map.class.getCanonicalName())
        .add(AbstractMap.class.getCanonicalName())
        .add(HashMap.class.getCanonicalName())
        .add(EnumMap.class.getCanonicalName())
        .add(TreeMap.class.getCanonicalName())
        .add(LinkedHashMap.class.getCanonicalName())
        .add(IdentityHashMap.class.getCanonicalName())
        .add(WeakHashMap.class.getCanonicalName())
        .build();

    /**
     * convert java to json
     *
     * @param project project
     * @param javaCode java code
     * @return {@link String}
     */
    public String javaToJson(Project project, String javaCode) {
        PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText("Dummy", JavaLanguage.INSTANCE, javaCode);
        Preconditions.checkNotNull(psiFile, "java code is empty!");
        Preconditions.checkArgument(psiFile instanceof PsiJavaFile, "java code is invalid!");

        PsiClass[] psiClasses = ((PsiJavaFile)psiFile).getClasses();
        Preconditions.checkArgument(ArrayUtils.isNotEmpty(psiClasses), "java code is invalid!");

        PsiClass psiClass = psiClasses[0];
        PsiField[] psiFields = psiClass.getAllFields();
        Map<String, PsiClass> namedClassMap = Maps.newHashMap();
        for (PsiClass aClass : psiClasses) {
            namedClassMap.put(aClass.getQualifiedName(), aClass);
            for (PsiClass innerClass : aClass.getAllInnerClasses()) {
                namedClassMap.put(innerClass.getQualifiedName(), innerClass);
            }
        }
        Map<String, Object> objectMap = buildJavaMap(namedClassMap, psiFields);
        return JsonUtil.toJson(objectMap, true);
    }

    private Map<String, Object> buildJavaMap(final Map<String, PsiClass> namedClassMap, PsiField[] psiFields) {
        Map<String, Object> objectMap = Maps.newTreeMap();
        for (PsiField psiField : psiFields) {
            // 非static
            if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
                continue;
            }
            objectMap.put(psiField.getName(), buildJavaValue(namedClassMap, psiField.getType()));
        }
        return objectMap;
    }

    private Object buildJavaValue(final Map<String, PsiClass> namedClassMap, PsiType psiType) {
        String fullType = psiType.getCanonicalText();
        if (TYPE_VALUE_MAP.containsKey(fullType)) {
            return TYPE_VALUE_MAP.get(fullType).get();
        } else if (StringUtil.anyStartWith(fullType, COLLECTION_LIST)) {
            PsiType elementType = ((PsiClassReferenceType)psiType).getParameters()[0];
            return new Object[] {buildJavaValue(namedClassMap, elementType), buildJavaValue(namedClassMap, elementType)};
        } else if (StringUtil.anyStartWith(psiType.getCanonicalText(), MAP_LIST)) {
            PsiType keyType = ((PsiClassReferenceType)psiType).getParameters()[0];
            PsiType valueType = ((PsiClassReferenceType)psiType).getParameters()[1];
            Map<String, Object> valueMap = Maps.newTreeMap();
            valueMap.put(String.valueOf(buildJavaValue(namedClassMap, keyType)), buildJavaValue(namedClassMap, valueType));
            valueMap.put(String.valueOf(buildJavaValue(namedClassMap, keyType)), buildJavaValue(namedClassMap, valueType));
            return valueMap;
        } else if (namedClassMap.containsKey(psiType.getCanonicalText())) {
            return buildJavaMap(namedClassMap, namedClassMap.get(psiType.getCanonicalText()).getAllFields());
        } else {
            return null;
        }
    }

    /**
     * convert json to java
     *
     * @param project project
     * @param jsonCode json code
     * @return {@link String}
     */
    public String jsonToJava(Project project, String jsonCode) throws Exception {

        Preconditions.checkArgument(StringUtils.startsWithAny(StringUtils.strip(jsonCode), "{", "["), "json code is invalid!");
        JsonNode jsonNode = JsonUtil.fromJson(jsonCode);
        Preconditions.checkArgument(jsonNode instanceof ArrayNode || jsonNode instanceof ObjectNode, "json code is invalid!");

        JavaBeanBO javaBean = new JavaBeanBO();
        Set<String> importSet = Sets.newHashSet();
        Set<JavaBeanBO> innerBeanSet = Sets.newLinkedHashSet();
        if (jsonNode instanceof ArrayNode) {
            Iterator<JsonNode> elementIterator = jsonNode.elements();
            if (elementIterator.hasNext()) {
                javaBean.setArrayType(getBeanFieldType(elementIterator.next(), importSet, innerBeanSet));
            } else {
                javaBean.setArrayType(Object.class.getSimpleName());
            }
            importSet.add(String.format(IMPORT_TEMPLATE, ArrayList.class.getCanonicalName()));
            javaBean.setIsArray(true);
        } else {
            javaBean = buildJavaBean(jsonNode, importSet, innerBeanSet, true);
        }

        String template = ResourceUtil.loadText(ResourceUtil.getResource(this.getClass().getClassLoader(),
            "template", "json-to-java-file.vm"));
        VelocityContext context = new VelocityContext();
        context.put("data", javaBean);
        context.put("author", System.getProperty("user.name"));
        context.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern(DateUtil.ALIBABA_DATE_TEMPLATE)));
        StringWriter writer = new StringWriter();
        Velocity.evaluate(context, writer, "", template);

        return writer.toString();
    }

    /**
     * build java bean
     *
     * @param jsonNode json node
     * @param importSet imports
     * @param innerBeanSet inner beans
     * @param isRoot is root bean
     * @return {@link JavaBeanBO}
     */
    private JavaBeanBO buildJavaBean(JsonNode jsonNode, Set<String> importSet, Set<JavaBeanBO> innerBeanSet, boolean isRoot) {
        JavaBeanBO javaBean = new JavaBeanBO();
        Set<JavaBeanFieldBO> fieldSet = Sets.newLinkedHashSet();
        javaBean.setFieldSet(fieldSet);
        javaBean.setImportSet(importSet);
        Iterator<String> fieldIterator = jsonNode.fieldNames();
        while (fieldIterator.hasNext()) {
            String key = fieldIterator.next();
            JsonNode value = jsonNode.get(key);
            fieldSet.add(new JavaBeanFieldBO(getBeanFieldType(value, importSet, innerBeanSet), key));
        }
        if (isRoot) {
            javaBean.setClassName("Dummy");
            javaBean.setInnerBeanSet(innerBeanSet);
        } else {
            Optional<JavaBeanBO> beanOpt = innerBeanSet.stream().filter(b -> b.equals(javaBean)).findAny();
            if (beanOpt.isPresent()) {
                javaBean.setClassName("InnerClass" + beanOpt.get().getIndex());
            } else {
                javaBean.setIndex(innerBeanSet.size() + 1);
                javaBean.setClassName("InnerClass" + (innerBeanSet.size() + 1));
                innerBeanSet.add(javaBean);
            }
        }
        return javaBean;
    }

    private String getBeanFieldType(JsonNode jsonNode, Set<String> importSet, Set<JavaBeanBO> innerBeanSet) {
        if (jsonNode instanceof ArrayNode) {
            Iterator<JsonNode> elementIterator = jsonNode.elements();
            importSet.add(String.format(IMPORT_TEMPLATE, List.class.getCanonicalName()));
            if (elementIterator.hasNext()) {
                JsonNode elementNode = elementIterator.next();
                return "List<" + getBeanFieldType(elementNode, importSet, innerBeanSet) + ">";
            } else {
                return "List<Object>";
            }
        } else if (jsonNode instanceof ObjectNode || jsonNode instanceof POJONode) {
            JavaBeanBO javaBean = buildJavaBean(jsonNode, importSet, innerBeanSet, false);
            return javaBean.getClassName();
        } else if (jsonNode instanceof TextNode) {
            return buildTextNodeFieldType((TextNode)jsonNode, importSet);
        } else if (jsonNode instanceof BigIntegerNode) {
            importSet.add(String.format(IMPORT_TEMPLATE, BigInteger.class.getCanonicalName()));
            return BigInteger.class.getSimpleName();
        } else if (jsonNode instanceof BinaryNode) {
            return "Byte[]";
        } else if (jsonNode instanceof BooleanNode) {
            return Boolean.class.getSimpleName();
        } else if (jsonNode instanceof DecimalNode) {
            importSet.add(String.format(IMPORT_TEMPLATE, BigDecimal.class.getCanonicalName()));
            return BigDecimal.class.getSimpleName();
        } else if (jsonNode instanceof DoubleNode || jsonNode instanceof FloatNode) {
            return Double.class.getSimpleName();
        } else if (jsonNode instanceof ShortNode || jsonNode instanceof IntNode) {
            return Integer.class.getSimpleName();
        } else if (jsonNode instanceof LongNode) {
            return Long.class.getSimpleName();
        } else {
            return Object.class.getSimpleName();
        }
    }

    /**
     * build text node type
     *
     * @param textNode text node
     * @param importSet import set
     * @return {@link JavaBeanFieldBO}
     */
    private String buildTextNodeFieldType(TextNode textNode, Set<String> importSet) {
        String text = textNode.asText();
        if (text.matches(DateUtil.DATE_TIME_REGEX) || text.matches(DateUtil.DATE_REGEX)) {
            importSet.add(String.format(IMPORT_TEMPLATE, Date.class.getCanonicalName()));
            return Date.class.getSimpleName();
        }
        return String.class.getSimpleName();
    }

}
