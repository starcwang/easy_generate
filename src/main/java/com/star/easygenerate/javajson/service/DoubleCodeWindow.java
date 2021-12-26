package com.star.easygenerate.javajson.service;

import java.awt.*;

import javax.swing.*;

import com.intellij.diff.DiffDialogHints;
import com.intellij.diff.chains.DiffRequestChain;
import com.intellij.diff.impl.CacheDiffRequestChainProcessor;
import com.intellij.diff.impl.DiffRequestProcessor;
import com.intellij.diff.tools.util.base.HighlightPolicy;
import com.intellij.diff.tools.util.base.TextDiffSettingsHolder.TextDiffSettings;
import com.intellij.diff.util.DiffUserDataKeys;
import com.intellij.diff.util.DiffUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.WindowWrapper;
import com.intellij.openapi.ui.WindowWrapperBuilder;
import com.intellij.openapi.util.BooleanGetter;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.util.Consumer;
import com.intellij.util.ui.JBUI;
import com.intellij.vcsUtil.UIVcsUtilKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 双代码窗口
 *
 * @author wangchao
 * @date 2021/12/26
 */
public class DoubleCodeWindow {
    @Nullable
    private final Project myProject;
    @NotNull
    private final DiffDialogHints myHints;
    @NotNull
    private final DiffRequestChain myRequestChain;

    private DiffRequestProcessor myProcessor;
    private WindowWrapper myWrapper;

    public DoubleCodeWindow(@Nullable Project project, @NotNull DiffRequestChain requestChain, @NotNull DiffDialogHints hints) {
        myProject = project;
        myHints = hints;
        myRequestChain = requestChain;
    }

    private void init() {
        if (myWrapper != null) {
            return;
        }

        myProcessor = createProcessor();

        String dialogGroupKey = myProcessor.getContextUserData(DiffUserDataKeys.DIALOG_GROUP_KEY);
        if (dialogGroupKey == null) {
            dialogGroupKey = "DiffContextDialog";
        }

        myWrapper = new WindowWrapperBuilder(DiffUtil.getWindowMode(myHints), new MyPanel(myProcessor.getComponent()))
            .setProject(myProject)
            .setParent(myHints.getParent())
            .setDimensionServiceKey(dialogGroupKey)
            .setPreferredFocusedComponent(() -> myProcessor.getPreferredFocusedComponent())
            .setOnShowCallback(() -> myProcessor.updateRequest())
            .build();
        myWrapper.setImages(DiffUtil.Lazy.DIFF_FRAME_ICONS);
        Disposer.register(myWrapper, myProcessor);

        Consumer<WindowWrapper> wrapperHandler = myHints.getWindowConsumer();
        if (wrapperHandler != null) {
            wrapperHandler.consume(myWrapper);
        }
    }

    public void show() {
        init();
        myWrapper.show();
    }

    @NotNull
    private DiffRequestProcessor createProcessor() {

        HighlightPolicy historyHighlightPolicy = TextDiffSettings.getSettings().getHighlightPolicy();
        TextDiffSettings.getSettings().setHighlightPolicy(HighlightPolicy.DO_NOT_HIGHLIGHT);

        return new MyCacheDiffRequestChainProcessor(myProject, myRequestChain, historyHighlightPolicy);
    }

    private WindowWrapper getWrapper() {
        return myWrapper;
    }

    private class MyCacheDiffRequestChainProcessor extends CacheDiffRequestChainProcessor {
        private final HighlightPolicy highlightPolicy;

        MyCacheDiffRequestChainProcessor(@Nullable Project project, @NotNull DiffRequestChain requestChain,
            HighlightPolicy highlightPolicy) {
            super(project, requestChain);
            this.highlightPolicy = highlightPolicy;

            // remove toolbar
            super.myToolbarWrapper.removeAll();
        }

        @Override
        protected void setWindowTitle(@NotNull String title) {
            getWrapper().setTitle(title);
        }

        @Override
        protected void onAfterNavigate() {
            DiffUtil.closeWindow(getWrapper().getWindow(), true, true);
        }

        @Override
        public <T> @Nullable T getContextUserData(@NotNull Key<T> key) {
            return super.getContextUserData(key);
        }

        @Override
        protected void onDispose() {
            super.onDispose();
            // reset history highlight settings
            TextDiffSettings.getSettings().setHighlightPolicy(highlightPolicy);
        }
    }

    private static class MyPanel extends JPanel {
        MyPanel(@NotNull JComponent content) {
            super(new BorderLayout());
            add(content, BorderLayout.CENTER);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension windowSize = DiffUtil.getDefaultDiffWindowSize();
            Dimension size = super.getPreferredSize();
            return new Dimension(Math.max(windowSize.width, size.width), Math.max(windowSize.height, size.height));
        }
    }
}