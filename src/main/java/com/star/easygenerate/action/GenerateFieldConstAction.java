package com.star.easygenerate.action;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
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
import com.intellij.psi.codeStyle.CodeStyleManager;
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

        // 遍历所有类
        List<PsiClass> classElements = getFromChildren(psiJavaFile, PsiClass.class);
        for (PsiClass classElement : classElements) {

            // 寻找类中的属性
            List<PsiField> fieldElements = getFromChildren(classElement, PsiField.class);
            Set<String> existsFieldNames = fieldElements.stream().map(NavigationItem::getName).collect(Collectors.toSet());

            List<PsiElement> constElements = Lists.newArrayList();
            for (PsiField fieldElement : fieldElements) {
                String name = fieldElement.getName();
                String constFieldName = "CONST_" + WordUtils.toUpperCase(name);
                // 跳过已存在的
                if (existsFieldNames.contains(constFieldName)) {
                    continue;
                }

                // 跳过static修饰的
                PsiModifierList modifierList = fieldElement.getModifierList();
                if (modifierList != null && modifierList.hasModifierProperty(PsiModifier.STATIC)) {
                    continue;
                }

                // 生成属性
                String fieldText = String.format("/** the constant of field {@link %s#%s} */\n public static final String %s = \"%s\";\n",
                    classElement.getName(), name, constFieldName, name);
                PsiField constField = factory.createFieldFromText(fieldText, null);
                constElements.add(constField);
            }

            write(project, classElement, constElements);
        }
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

                PsiElement first = null;
                for (PsiElement constElement : constElements) {
                    PsiElement target = classElement.addBefore(constElement, lastRbrace);
                    if (first == null) {
                        first = target;
                    }
                }
                if (first == null) {
                    return;
                }

                // 格式化文档注释
                CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
                int startOffset = first.getTextOffset();
                int endOffset = lastRbrace.getTextOffset() + lastRbrace.getText().length();
                codeStyleManager.reformatText(classElement.getContainingFile(), startOffset, endOffset + 1);
            });
    }

    @SuppressWarnings("unchecked")
    private <T extends PsiElement> List<T> getFromChildren(PsiElement psiElement, Class<T> targetElementClass) {
        return Arrays.stream(psiElement.getChildren())
            .filter(targetElementClass::isInstance)
            .map(psiElement1 -> (T)psiElement1)
            .collect(Collectors.toList());
    }
}
