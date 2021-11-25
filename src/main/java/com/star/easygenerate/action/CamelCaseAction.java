package com.star.easygenerate.action;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.CaretAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.actions.TextComponentEditorAction;
import com.intellij.openapi.util.TextRange;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

/**
 * @author wangchao
 * @date 2021/11/17
 */
public class CamelCaseAction extends TextComponentEditorAction {

    /** camelCase */
    private static final int CAMEL_CASE = 1;
    /** kebab-case */
    private static final int KEBAB_CASE = 2;
    /** lower_snake_case */
    private static final int LOWER_SNAKE_CASE = 3;
    /** UPPER_SNAKE_CASE */
    private static final int UPPER_SNAKE_CASE = 4;
    /** PascalCase */
    private static final int PASCAL_CASE = 5;
    /** lower space case */
    private static final int LOWER_SPACE_CASE = 6;
    /** UPPER SPACE CASE */
    private static final int UPPER_SPACE_CASE = 7;

    /** 支持的样式数量 */
    private static final int CASE_COUNT = 7;

    /** 分隔正则表达式 */
    private static final String SPLIT_REGEX = "[- _]+";

    public CamelCaseAction() {
        super(new Handler());
    }

    private static class Handler extends EditorWriteActionHandler {
        @Override
        public void executeWriteAction(final Editor editor, @Nullable Caret caret, DataContext dataContext) {

            // 创建临时鼠标动作实现
            CaretAction caretAction = c -> {
                // 是否有默认选中
                if (!c.hasSelection()) {
                    c.selectWordAtCaret(true);
                }

                // 记录当前选中位置
                int selectionStartOffset = c.getSelectionStart();
                int selectionEndOffset = c.getSelectionEnd();
                int originalTextLength = selectionEndOffset - selectionStartOffset;
                VisualPosition selectionStartPosition = c.getSelectionStartPosition();
                VisualPosition selectionEndPosition = c.getSelectionEndPosition();
                c.removeSelection();

                // 转换前字符串
                String originalText = editor.getDocument()
                    .getText(new TextRange(selectionStartOffset, selectionEndOffset));
                // 转换后字符串
                String targetText = convertText(originalText);
                int endOffset = selectionEndOffset - originalTextLength + targetText.length();

                // 替换字符串
                editor.getDocument().replaceString(selectionStartOffset, selectionEndOffset, targetText);

                // 重新放置光标位置为新字符串最后
                VisualPosition visualPosition = new VisualPosition(selectionStartPosition.line,
                    selectionStartPosition.column + targetText.length(), selectionStartPosition.leansRight);
                c.moveToVisualPosition(visualPosition);

                // 设置选中新的字符串
                c.setSelection(selectionStartPosition, selectionStartOffset, selectionEndPosition, endOffset);
            };

            // 执行动作
            if (caret == null) {
                editor.getCaretModel().runForEachCaret(caretAction);
            } else {
                caretAction.perform(caret);
            }
        }

        /**
         * convert selection
         *
         * @param text 文本
         * @return {@link String}
         */
        private String convertText(String text) {
            Pair<Integer, List<String>> splitCase = getSplitCase(text);

            String targetText = text;
            int targetCase = splitCase.getLeft();
            for (int i = 0; i < CASE_COUNT; i++) {
                targetCase = targetCase + 1;
                targetCase = targetCase > CASE_COUNT ? targetCase - CASE_COUNT : targetCase;
                targetText = buildTargetCase(splitCase.getRight(), targetCase);
                if (!Objects.equals(text, targetText)) {
                    break;
                }
            }

            // 转换
            return targetText;
        }

        /**
         * 构建目标字符串
         *
         * @param subList 分割列表
         * @param targetCase 目标情况
         * @return {@link String}
         */
        private String buildTargetCase(List<String> subList, int targetCase) {
            String targetText;
            switch (targetCase) {
                case CAMEL_CASE:
                    targetText = subList.stream().map(this::upperFirstChar).collect(Collectors.joining());
                    targetText = targetText.substring(0, 1).toLowerCase() + targetText.substring(1);
                    break;
                case PASCAL_CASE:
                    targetText = subList.stream().map(this::upperFirstChar).collect(Collectors.joining());
                    break;
                case KEBAB_CASE:
                    targetText = subList.stream().map(String::toLowerCase).collect(Collectors.joining("-"));
                    break;
                case LOWER_SNAKE_CASE:
                    targetText = subList.stream().map(String::toLowerCase).collect(Collectors.joining("_"));
                    break;
                case UPPER_SNAKE_CASE:
                    targetText = subList.stream().map(String::toUpperCase).collect(Collectors.joining("_"));
                    break;
                case LOWER_SPACE_CASE:
                    targetText = subList.stream().map(String::toLowerCase).collect(Collectors.joining(" "));
                    break;
                case UPPER_SPACE_CASE:
                    targetText = subList.stream().map(String::toUpperCase).collect(Collectors.joining(" "));
                    break;
                default:
                    targetText = String.join("", subList);
                    break;
            }
            return targetText;
        }

        /**
         * 首字母大写
         *
         * @param text 文本
         * @return {@link String}
         */
        private String upperFirstChar(String text) {
            return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
        }

        /**
         * 获取分割字符串以及当前类型
         *
         * @param text 文本
         * @return {@link Pair}<{@link Integer}, {@link List}<{@link String}>>
         */
        private Pair<Integer, List<String>> getSplitCase(String text) {
            int thisCase;
            String[] split;
            if (text.contains(StringUtils.SPACE)) {
                split = text.split(SPLIT_REGEX);
                if (isAllUpper(split)) {
                    thisCase = UPPER_SPACE_CASE;
                } else {
                    thisCase = LOWER_SPACE_CASE;
                }
            } else if (text.contains("-")) {
                split = text.split(SPLIT_REGEX);
                thisCase = KEBAB_CASE;
            } else if (text.contains("_")) {
                split = text.split(SPLIT_REGEX);
                if (isAllUpper(split)) {
                    thisCase = UPPER_SNAKE_CASE;
                } else {
                    thisCase = LOWER_SNAKE_CASE;
                }
            } else if (isAllUpper(text)) {
                split = text.split(SPLIT_REGEX);
                thisCase = UPPER_SNAKE_CASE;
            } else if (isAllLower(text)) {
                split = text.split(SPLIT_REGEX);
                thisCase = LOWER_SNAKE_CASE;
            } else if (isAllUpper(text.substring(0, 1))) {
                text = text.replaceAll("(?<=[^A-Z])[A-Z][^A-Z]", "_$0");
                text = text.replaceAll("[A-Z]{2,}", "_$0");
                text = text.replaceAll("_+", "_");
                split = text.split(SPLIT_REGEX);
                thisCase = PASCAL_CASE;
            } else {
                text = text.replaceAll("(?<=[^A-Z])[A-Z][^A-Z]", "_$0");
                text = text.replaceAll("[A-Z]{2,}", "_$0");
                text = text.replaceAll("_+", "_");
                split = text.split(SPLIT_REGEX);
                thisCase = CAMEL_CASE;
            }
            return Pair.of(thisCase, Lists.newArrayList(split));
        }

        /**
         * 判断时是否都是大写
         *
         * @param texts 文本
         * @return boolean
         */
        private boolean isAllUpper(String... texts) {
            for (String text : texts) {
                if (!text.matches("^[A-Z0-9]+$")) {
                    return false;
                }
            }
            return true;
        }

        /**
         * 判断时是否都是小写
         *
         * @param texts 文本
         * @return boolean
         */
        private boolean isAllLower(String... texts) {
            for (String text : texts) {
                if (!text.matches("^[a-z0-9]+$")) {
                    return false;
                }
            }
            return true;
        }

    }
}
