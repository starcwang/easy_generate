package com.star.easygenerate.javajson.action;

import java.awt.*;

import javax.swing.*;

import com.intellij.diff.util.DiffUtil;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.ui.WindowWrapper;
import com.intellij.openapi.ui.WindowWrapper.Mode;
import com.intellij.openapi.ui.WindowWrapperBuilder;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.HyperlinkLabel;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.panels.Wrapper;
import com.star.easygenerate.javajson.ui.LanguageTextComponent;
import org.apache.commons.lang3.RandomStringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * @author wangchao
 * @date 2021/12/18
 */
public class Json2JavaAction extends DumbAwareAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();

        // java wrapper
        LanguageTextComponent javaField = new LanguageTextComponent(JavaLanguage.INSTANCE, project, "", false);
        JComponent javaComponent = createCodeComponent("Java", javaField);
        // json wrapper
        LanguageTextComponent jsonField = new LanguageTextComponent(JsonLanguage.INSTANCE, project, "", false);
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
        JButton javaToJsonButton = createJavaToJsonButton(notificationPanel, javaField, jsonField);
        // java to json button
        JButton jsonToJavaButton = createJsonToJavaButton(notificationPanel, jsonField, javaField);
        Splitter buttonSplitter = new JBSplitter(false);
        buttonSplitter.setFirstComponent(javaToJsonButton);
        buttonSplitter.setSecondComponent(jsonToJavaButton);

        // build bottom
        Splitter bottomSplitter = new JBSplitter(false, 0.8f);
        bottomSplitter.setFirstComponent(new JPanel());
        bottomSplitter.setSecondComponent(buttonSplitter);

        // mainComponent
        JComponent mainComponent = createMainComponent(notificationPanel, codeSplitter, bottomSplitter);

        // build window
        WindowWrapperBuilder builder = new WindowWrapperBuilder(Mode.FRAME, mainComponent);
        builder.setTitle("Java ⇌ Json");
        WindowWrapper windowWrapper = builder.build();
        windowWrapper.show();
    }

    private JComponent createCodeComponent(String title, LanguageTextComponent textComponent) {
        JComponent titleComponent = DiffUtil.createTitle(title);
        Wrapper javaWrapper = new Wrapper();
        javaWrapper.add(titleComponent, BorderLayout.NORTH);
        javaWrapper.add(textComponent, BorderLayout.CENTER);
        return javaWrapper;
    }

    private JButton createJsonToJavaButton(EditorNotificationPanel notificationPanel, LanguageTextComponent jsonTextComponent,
        LanguageTextComponent javaTextComponent) {
        JButton button = new JButton();
        button.addActionListener(event -> {
            jsonTextComponent.setText(RandomStringUtils.randomAlphanumeric(32));
            notificationPanel.setVisible(true);
        });
        button.setText("json to java");
        return button;
    }

    private JButton createJavaToJsonButton(EditorNotificationPanel notificationPanel, LanguageTextComponent javaTextComponent,
        LanguageTextComponent jsonTextComponent) {
        JButton button = new JButton();
        button.addActionListener(event -> {
            javaTextComponent.setText(RandomStringUtils.randomAlphanumeric(32));
            notificationPanel.setVisible(true);
        });
        button.setText("java to json");
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
