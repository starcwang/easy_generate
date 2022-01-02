package com.star.easygenerate.javajson.action;

import java.awt.*;

import javax.swing.*;

import com.intellij.diff.util.DiffUtil;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportList;
import com.intellij.psi.PsiJavaFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.panels.Wrapper;
import com.star.easygenerate.javajson.service.JavaJsonConverterService;
import com.star.easygenerate.javajson.ui.JavaJsonDialogWrapper;
import com.star.easygenerate.javajson.ui.LanguageTextComponent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author wangchao
 * @date 2021/12/18
 */
public class JavaJsonConvertAction extends DumbAwareAction {
    /** 转换器服务 */
    private final JavaJsonConverterService converterService = ServiceManager.getService(JavaJsonConverterService.class);

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        PsiElement psiElement = e.getData(CommonDataKeys.PSI_ELEMENT);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        String javaCode = "";
        String jsonCode = "";

        if (psiElement instanceof PsiClass) {
            javaCode = psiElement.getText();
            PsiJavaFile psiJavaFile = (PsiJavaFile)psiElement.getContainingFile();
            PsiImportList psiImportList = psiJavaFile.getImportList();
            if (psiImportList != null) {
                javaCode = psiImportList.getText() + "\n\n" + javaCode;
            }
        }
        if (psiFile != null && JsonLanguage.INSTANCE.equals(psiFile.getLanguage())) {
            jsonCode = psiFile.getText();
        }

        // java wrapper
        LanguageTextComponent javaField = new LanguageTextComponent(JavaLanguage.INSTANCE, project, javaCode, false);
        JComponent javaComponent = createCodeComponent("Java", javaField);
        // json wrapper
        LanguageTextComponent jsonField = new LanguageTextComponent(JsonLanguage.INSTANCE, project, jsonCode, false);
        JComponent jsonComponent = createCodeComponent("Json", jsonField);
        // code wrapper
        Splitter codeSplitter = new JBSplitter(false);
        codeSplitter.setFirstComponent(javaComponent);
        codeSplitter.setSecondComponent(jsonComponent);

        // build notification panel
        EditorNotificationPanel notificationPanel = new EditorNotificationPanel();
        notificationPanel.text("");
        HyperlinkLabel link = notificationPanel.createActionLabel("hide", () -> notificationPanel.setVisible(false));
        link.setToolTipText("hide");
        notificationPanel.setVisible(false);

        // java to json button
        JButton javaToJsonButton = createJavaToJsonButton(project, notificationPanel, javaField, jsonField);
        // java to json button
        JButton jsonToJavaButton = createJsonToJavaButton(project, notificationPanel, jsonField, javaField);
        Splitter buttonSplitter = new JBSplitter(false);
        buttonSplitter.setFirstComponent(javaToJsonButton);
        buttonSplitter.setSecondComponent(jsonToJavaButton);

        // init
        if (StringUtils.isNotBlank(javaField.getText())) {
            try {
                jsonField.setText(converterService.javaToJson(project, javaField.getText()));
            } catch (Exception ex) {
                notificationPanel.setText(ex.getMessage());
                notificationPanel.setVisible(true);
            }
        } else if (StringUtils.isNotBlank(jsonField.getText())) {
            try {
                javaField.setText(converterService.jsonToJava(project, jsonField.getText()));
            } catch (Exception ex) {
                notificationPanel.setText(ex.getMessage());
                notificationPanel.setVisible(true);
            }
        }

        // build bottom
        Splitter bottomSplitter = new JBSplitter(false, 0.8f);
        bottomSplitter.setFirstComponent(new JPanel());
        bottomSplitter.setSecondComponent(buttonSplitter);

        // mainComponent
        JComponent mainComponent = createMainComponent(notificationPanel, codeSplitter, bottomSplitter);

        // build window
        JavaJsonDialogWrapper wrapper = new JavaJsonDialogWrapper("Java ⇌ Json", mainComponent);
        wrapper.show();
    }

    private JComponent createCodeComponent(String title, LanguageTextComponent textComponent) {
        JComponent titleComponent = DiffUtil.createTitle(title);
        Wrapper javaWrapper = new Wrapper();
        javaWrapper.add(titleComponent, BorderLayout.NORTH);
        javaWrapper.add(textComponent, BorderLayout.CENTER);
        return javaWrapper;
    }

    private JButton createJsonToJavaButton(Project project, EditorNotificationPanel notificationPanel,
        LanguageTextComponent jsonTextComponent, LanguageTextComponent javaTextComponent) {
        JButton button = new JButton();
        button.addActionListener(event -> {
            try {
                javaTextComponent.setText(converterService.jsonToJava(project, jsonTextComponent.getText()));
            } catch (Exception e) {
                notificationPanel.setText(e.getMessage());
                notificationPanel.setVisible(true);
            }
        });
        button.setText("⇇json to java⇇");
        return button;
    }

    private JButton createJavaToJsonButton(Project project, EditorNotificationPanel notificationPanel,
        LanguageTextComponent javaTextComponent, LanguageTextComponent jsonTextComponent) {
        JButton button = new JButton();
        button.addActionListener(event -> {
            try {
                jsonTextComponent.setText(converterService.javaToJson(project, javaTextComponent.getText()));
            } catch (Exception e) {
                notificationPanel.setText(e.getMessage());
                notificationPanel.setVisible(true);
            }
        });
        button.setText("⇉java to json⇉");
        return button;
    }

    private JComponent createMainComponent(JComponent topComponent, JComponent codeComponent, JComponent bottomComponent) {
        Wrapper mainWrapper = new Wrapper();
        mainWrapper.add(topComponent, BorderLayout.NORTH);
        mainWrapper.add(codeComponent, BorderLayout.CENTER);
        mainWrapper.add(bottomComponent, BorderLayout.SOUTH);
        return mainWrapper;
    }
}
