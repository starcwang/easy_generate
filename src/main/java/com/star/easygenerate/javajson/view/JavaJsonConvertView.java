package com.star.easygenerate.javajson.view;

import java.awt.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import com.intellij.json.JsonFileType;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorComponentImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.LanguageTextField;
import com.intellij.ui.components.panels.Wrapper;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nullable;

/**
 * @author wangchao
 * @date 2021/12/26
 */
public class JavaJsonConvertView  {

    private JFrame frame;
    private JPanel panel;
    private JButton button1;
    private JPanel codePanel;
    private final Project project;

    public JavaJsonConvertView(Project project) {
        this.project = project;
    }

    private void createUIComponents() {

        // 创建及设置窗口
        frame = new JFrame("HelloWorldSwing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LanguageTextField javaField = new LanguageTextField(JavaLanguage.INSTANCE, project, "javajavajava", false);
        LanguageTextField jsonField = new LanguageTextField(JsonLanguage.INSTANCE, project, "jsonjsonjson", false);

        codePanel = new Wrapper();

        codePanel.add(javaField);
        codePanel.add(jsonField);

        panel = new JPanel();
        panel.add(codePanel);

        frame.getContentPane().add(panel);
    }

    public void show() {
        frame.pack();
        frame.setVisible(true);
    }
}
