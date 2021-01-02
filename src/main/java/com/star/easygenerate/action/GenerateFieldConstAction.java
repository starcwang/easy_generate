package com.star.easygenerate.action;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.intellij.codeInsight.generation.ClassMemberWithElement;
import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.ide.util.MemberChooser;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaTokenType;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiJavaToken;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiModifierList;
import com.star.easygenerate.util.WordUtils;
import org.jetbrains.annotations.NotNull;

/**
 * 生成常量
 *
 * @author wangchao
 * @date 2020/12/13
 */
public class GenerateFieldConstAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
        // 获取project
        Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        // 获取factory
        PsiElementFactory factory = PsiElementFactory.SERVICE.getInstance(project);

        // 判断java文件
        PsiFile psiFile = anActionEvent.getData(CommonDataKeys.PSI_FILE);
        if (!(psiFile instanceof PsiJavaFile)) {
            return;
        }
        PsiJavaFile psiJavaFile = (PsiJavaFile)psiFile;

        // 找到所有类
        List<PsiClass> classElements = getAllClass(psiJavaFile);

        List<PsiField> allPsiFields = Lists.newArrayList();
        for (PsiClass classElement : classElements) {
            List<PsiField> fieldList = getFromChildren(classElement, PsiField.class);
            Set<String> existsFieldNames = fieldList.stream().map(NavigationItem::getName).collect(Collectors.toSet());
            fieldList.removeIf(f -> {
                String name = f.getName();
                String constFieldName = getConstName(name);
                if (existsFieldNames.contains(constFieldName)) {
                    return true;
                }
                PsiModifierList modifierList = f.getModifierList();
                return modifierList != null && modifierList.hasModifierProperty(PsiModifier.STATIC);
            });
            allPsiFields.addAll(fieldList);
        }

        // 手动选取类
        List<ClassMemberWithElement> selectedFields = getSelectedFields(project, allPsiFields);

        if (selectedFields == null || selectedFields.isEmpty()) {
            return;
        }

        // 生成属性
        Map<PsiClass, List<PsiElement>> writeMap = Maps.newHashMap();
        for (ClassMemberWithElement selectedField : selectedFields) {
            PsiField psiField = (PsiField)selectedField.getElement();
            PsiClass psiClass = (PsiClass)psiField.getContext();
            if (psiClass == null) {
                continue;
            }
            String fieldText = MessageFormat.format("/** the constant of field '{'@link {0}#{1}'}' */\n"
                    + " public static final String {2} = \"{1}\";\n",
                psiClass.getName(), psiField.getName(), getConstName(psiField.getName()));
            PsiField constField = factory.createFieldFromText(fieldText, null);
            writeMap.computeIfAbsent(psiClass, psiClass1 -> Lists.newArrayList()).add(constField);
        }

        // 写入
        writeMap.forEach((k, v) -> write(project, k, v));
    }

    /**
     * 生成常量名
     *
     * @param name 名字
     * @return {@link String}
     */
    private String getConstName(String name) {
        return "CONST_" + WordUtils.toUpperCase(name);
    }

    /**
     * 获取选中字段
     *
     * @param project 项目
     * @param allFields 所有字段
     * @return {@link List<ClassMemberWithElement>}
     */
    private List<ClassMemberWithElement> getSelectedFields(Project project, List<PsiField> allFields) {
        ClassMemberWithElement[] members = new ClassMemberWithElement[allFields.size()];
        for (int i = 0, allFieldsSize = allFields.size(); i < allFieldsSize; i++) {
            members[i] = new PsiFieldMember(allFields.get(i));
        }

        MemberChooser<ClassMemberWithElement> chooser = new MemberChooser<>(members, true, true, project);
        chooser.setTitle("请选择生成常量的字段");
        chooser.setCopyJavadocVisible(true);
        if (chooser.showAndGet()) {
            return chooser.getSelectedElements();
        }
        return Lists.newArrayList();
    }

    /**
     * 写入代码
     */
    private void write(Project project, PsiClass classElement, List<PsiElement> constElements) {
        // 写入代码
        WriteCommandAction.writeCommandAction(project).run(
            () -> {
                if (constElements.isEmpty()) {
                    return;
                }

                // 寻找最后一个花括号，追加到类最后面
                List<PsiJavaToken> javaTokens = getFromChildren(classElement, PsiJavaToken.class);
                PsiJavaToken lastRbrace = null;
                for (PsiJavaToken javaToken : javaTokens) {
                    if (JavaTokenType.RBRACE.equals(javaToken.getTokenType())) {
                        lastRbrace = javaToken;
                    }
                }
                if (lastRbrace == null) {
                    return;
                }

                for (PsiElement constElement : constElements) {
                    classElement.addBefore(constElement, lastRbrace);
                }
            });
    }

    /**
     * 得到所有PsiClass
     *
     * @param psiElement psi元素
     * @return {@link List<PsiClass>}
     */
    private List<PsiClass> getAllClass(PsiElement psiElement) {
        List<PsiClass> res = Lists.newArrayList();
        List<PsiClass> list = Arrays.stream(psiElement.getChildren())
            .filter(PsiClass.class::isInstance)
            .map(e -> (PsiClass)e)
            .collect(Collectors.toList());
        res.addAll(list);
        for (PsiClass psiClass : list) {
            res.addAll(getAllClass(psiClass));
        }
        return res;
    }

    @SuppressWarnings("unchecked")
    private <T extends PsiElement> List<T> getFromChildren(PsiElement psiElement, Class<T> targetElementClass) {
        return Arrays.stream(psiElement.getChildren())
            .filter(targetElementClass::isInstance)
            .map(psiElement1 -> (T)psiElement1)
            .collect(Collectors.toList());
    }

    @Override
    public boolean startInTransaction() {
        return true;
    }
}
