package com.star.easygenerate.javajson.ui;

import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.Border;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * java json 对话框
 *
 * @author wangchao
 * @date 2022/01/02
 */
public class JavaJsonDialogWrapper extends DialogWrapper {
    @NotNull
    private final JComponent myComponent;

    public JavaJsonDialogWrapper(@NotNull String title, @NotNull JComponent component) {
        super(true);
        myComponent = component;

        setTitle(StringUtil.notNullize(title));

        setModal(false);
        init();
    }

    @Nullable
    @Override
    protected Border createContentPaneBorder() {
        return null;
    }

    @Override
    protected JComponent createCenterPanel() {
        return myComponent;
    }

    @Override
    protected Action @NotNull [] createActions() {
        return new Action[0];
    }

    @Nullable
    @Override
    protected JComponent createSouthPanel() {
        return null;
    }

    @Nullable
    @Override
    protected ActionListener createCancelAction() {
        // Prevent closing by <Esc>
        return null;
    }

}