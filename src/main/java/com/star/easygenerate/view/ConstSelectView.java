package com.star.easygenerate.view;


import javax.swing.*;

import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * @author wangchao
 * @date 2020/12/20
 */
public class ConstSelectView extends DialogWrapper {

    private JPanel jPanel;

    public ConstSelectView() {
        super(false);
        init();
        setTitle("请选择属性");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return jPanel;
    }

    private void createUIComponents() {
        jPanel = new JPanel();
    }
}
