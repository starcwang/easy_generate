package com.star.easygenerate.javajson.ui;

import com.intellij.lang.Language;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.ui.LanguageTextField;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author wangchao
 * @date 2021/12/26
 */
public class LanguageTextComponent extends LanguageTextField {

    public LanguageTextComponent(Language language, @Nullable Project project, @NotNull String value, boolean oneLineMode) {
        super(language, project, value, oneLineMode);

        addNotify();
    }

    @Override
    protected EditorEx createEditor() {
        // show line numbers
        EditorEx editor = super.createEditor();
        editor.getSettings().setLineNumbersShown(true);
        editor.setHorizontalScrollbarVisible(true);
        editor.setVerticalScrollbarVisible(true);
        return editor;
    }
}
