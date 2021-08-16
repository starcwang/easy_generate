package com.star.easygenerate.view;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.JBList;
import com.star.easygenerate.config.EasyGenerateConfig;
import com.star.easygenerate.config.EasyGenerateConfigComponent;
import org.jetbrains.annotations.Nullable;

/**
 * 属性添加视图
 *
 * @author wangchao
 * @date 2021/07/11
 */
public class AboveFieldView extends DialogWrapper {
    private JPanel contentPane;
    private JTextArea codeTextArea;
    private JList<Entry<String, String>> innerVariablesList;

    /** 内部变量映射 */
    private static final List<Entry<String, String>> INNER_VARIABLE_LIST = ImmutableList
        .<Entry<String, String>>builder()
        .add(new SimpleEntry<>("fieldName", "field name"))
        .add(new SimpleEntry<>("fieldType", "field type"))
        .build();

    public AboveFieldView() {
        super(false);
        init();
        setTitle("Add Something Above Fields");
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public String getCodeText() {
        return codeTextArea.getText();
    }

    private void createUIComponents() {
        // 内置变量初始化
        innerVariablesList = new JBList<>(new CollectionListModel<>(Lists.newArrayList()));
        innerVariablesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        innerVariablesList.setCellRenderer(new ListCellRendererWrapper<Entry<String, String>>() {
            @Override
            public void customize(JList list, Entry<String, String> value, int index, boolean selected,
                boolean hasFocus) {
                setText(value.getKey() + " : " + value.getValue());
            }
        });
        innerVariablesList.setSelectedIndex(0);
        innerVariablesList.setModel(new CollectionListModel<>(INNER_VARIABLE_LIST));

        // 代码区域初始化
        codeTextArea = new JTextArea();
        EasyGenerateConfig config = ServiceManager.getService(EasyGenerateConfigComponent.class).getState();
        codeTextArea.setText(config.getAddAboveFields());
    }
}
