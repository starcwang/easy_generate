package com.star.easygenerate.action;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.star.easygenerate.view.AboveFieldView;
import org.jetbrains.annotations.NotNull;

/**
 * 生成常量
 *
 * @author wangchao
 * @date 2020/12/13
 */
public class AddAboveFieldAction extends AnAction {

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

        // 在类上
        PsiElement psiElement = anActionEvent.getData(LangDataKeys.PSI_ELEMENT);
        if (!(psiElement instanceof PsiClass)) {
            return;
        }
        PsiClass psiClass = (PsiClass)psiElement;
        AboveFieldView view = new AboveFieldView();
        if (view.showAndGet()) {
            String codeText = view.getCodeText();
            List<PsiField> fieldElements = getFromChildren(psiClass, PsiField.class);
            for (PsiField fieldElement : fieldElements) {
                PsiElement something = factory.createIdentifier(codeText);
                write(project, fieldElement, something);
            }
        }
    }

    /**
     * 写入代码
     */
    private void write(Project project, PsiField fieldElement, PsiElement something) {
        // 写入代码
        WriteCommandAction.writeCommandAction(project).run(
            () -> {
                fieldElement.addBefore(fieldElement.getFirstChild(), something);

                // 格式化文档注释
                CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
                int startOffset = fieldElement.getFirstChild().getTextOffset();
                int endOffset = fieldElement.getLastChild().getTextOffset() + something.getText().length();
                codeStyleManager.reformatText(fieldElement.getContainingFile(), startOffset, endOffset + 1);
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
