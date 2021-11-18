package com.star.easygenerate.action;

import java.util.Random;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.star.easygenerate.config.EasyGenerateConfig;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;

/**
 * @author wangchao
 * @date 2021/11/18
 */
public class GenerateAllSetter extends PsiElementBaseIntentionAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element)
        throws IncorrectOperationException {

        System.out.println("hello");

    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return element instanceof PsiIdentifier && element.getParent() instanceof PsiLocalVariable;
    }

    @Override
    public @NotNull @Nls(capitalization = Capitalization.Sentence) String getFamilyName() {
        return getText();
    }

    @NotNull
    @Override
    public String getText() {
        return "Generate all setter";
    }
}
