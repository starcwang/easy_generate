package com.star.easygenerate.setter.action;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiLocalVariable;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiUtilBase;
import com.intellij.util.IncorrectOperationException;
import com.star.easygenerate.setter.model.SetterBO;
import com.star.easygenerate.setter.service.SetterService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nls.Capitalization;
import org.jetbrains.annotations.NotNull;

/**
 * @author wangchao
 * @date 2021/11/18
 */
public abstract class BaseGenerateAllSetter extends PsiElementBaseIntentionAction {
    private static final Logger LOGGER = Logger.getInstance(BaseGenerateAllSetter.class);

    private final SetterService setterService = ServiceManager.getService(SetterService.class);

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element)
        throws IncorrectOperationException {

        // 准备
        PsiLocalVariable psiLocalVariable = (PsiLocalVariable)element.getParent();
        PsiElement parentElement = psiLocalVariable.getParent();
        PsiFile psiFile = PsiUtilBase.getPsiFileInEditor(editor, project);
        if (!(psiFile instanceof PsiJavaFile)) {
            return;
        }
        PsiJavaFile psiJavaFile = (PsiJavaFile)psiFile;

        // 计算setter方法
        SetterBO setter = setterService.buildSetters(psiLocalVariable, withDefault());
        if (setter == null || CollectionUtils.isEmpty(setter.getSetterMethodList())) {
            return;
        }
        String setterCode = setter.getSetterCode();

        // 计算setter插入位置，并执行插入
        int startOffset = parentElement.getTextOffset() + parentElement.getTextLength();
        PsiDocumentManager psiDocumentManager = PsiDocumentManager.getInstance(project);
        Document document = psiDocumentManager.getDocument(psiJavaFile);
        if (document == null) {
            LOGGER.warn("document is null");
            return;
        }
        document.insertString(startOffset, setterCode);
        psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
        psiDocumentManager.commitDocument(document);
        FileDocumentManager.getInstance().saveDocument(document);

        // 格式化
        CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
        codeStyleManager.reformatText(psiJavaFile, startOffset, startOffset + setterCode.length());

        // 添加import
        String importCode = setter.getImportCode();
        if (StringUtils.isNoneBlank(importCode)) {
            PsiImportList psiImportList = psiJavaFile.getImportList();
            if (psiImportList == null) {
                return;
            }
            int importStart = psiImportList.getTextOffset() + psiImportList.getTextLength();

            document.insertString(importStart, importCode + "\n");
            psiDocumentManager.doPostponedOperationsAndUnblockDocument(document);
            psiDocumentManager.commitDocument(document);
            FileDocumentManager.getInstance().saveDocument(document);

            codeStyleManager.reformatText(psiJavaFile, importStart, importStart + importCode.length());
        }

    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return element instanceof PsiIdentifier && element.getParent() instanceof PsiLocalVariable;
    }

    @Override
    public @NotNull @Nls(capitalization = Capitalization.Sentence) String getFamilyName() {
        return getText();
    }

    @Override
    public @Nls(capitalization = Capitalization.Sentence) @NotNull String getText() {
        return getTitle();
    }

    /**
     * 获得提示文案
     *
     * @return IDE提示文案
     */
    protected abstract String getTitle();

    /**
     * 是否用默认值
     *
     * @return boolean
     */
    protected abstract boolean withDefault();

}
