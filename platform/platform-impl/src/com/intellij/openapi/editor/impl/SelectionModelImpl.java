// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.intellij.openapi.editor.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.SelectionEvent;
import com.intellij.openapi.editor.event.SelectionListener;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * 选择模型
 *
 * 选择模型提供了那些接口
 */
public class SelectionModelImpl implements SelectionModel {
  private static final Logger LOG = Logger.getInstance(SelectionModelImpl.class);

  /**
   * 选择器监听
   */
  private final List<SelectionListener> mySelectionListeners = ContainerUtil.createLockFreeCopyOnWriteList();
  /**
   * 编辑器
   */
  private final EditorImpl myEditor;

  /**
   * 文本属性
   */
  private TextAttributes myTextAttributes;

  public SelectionModelImpl(EditorImpl editor) {
    myEditor = editor;
  }

  /**
   * 是否是未知的范围
   * @see CaretImpl#setUnknownDirection(boolean)
   */
  public boolean isUnknownDirection() {
    return myEditor.getCaretModel().getCurrentCaret().isUnknownDirection();
  }

  /**
   * @see CaretImpl#setUnknownDirection(boolean)
   */
  public void setUnknownDirection(boolean unknownDirection) {
    myEditor.getCaretModel().getCurrentCaret().setUnknownDirection(unknownDirection);
  }

  @Override
  public @NotNull Editor getEditor() {
    return myEditor;
  }

  /**
   * 处理选择的变更
   * @param event
   */
  void fireSelectionChanged(SelectionEvent event) {
    TextRange[] oldRanges = event.getOldRanges();
    TextRange[] newRanges = event.getNewRanges();
    int count = Math.min(oldRanges.length, newRanges.length);
    for (int i = 0; i < count; i++) {
      TextRange oldRange = oldRanges[i];
      TextRange newRange = newRanges[i];
      int oldSelectionStart = oldRange.getStartOffset();
      int startOffset = newRange.getStartOffset();
      int oldSelectionEnd = oldRange.getEndOffset();
      int endOffset = newRange.getEndOffset();
      myEditor.repaint(Math.min(oldSelectionStart, startOffset), Math.max(oldSelectionStart, startOffset), false);
      myEditor.repaint(Math.min(oldSelectionEnd, endOffset), Math.max(oldSelectionEnd, endOffset), false);
    }
    TextRange[] remaining = oldRanges.length < newRanges.length ? newRanges : oldRanges;
    for (int i = count; i < remaining.length; i++) {
      TextRange range = remaining[i];
      myEditor.repaint(range.getStartOffset(), range.getEndOffset(), false);
    }

    broadcastSelectionEvent(event);
  }

  /**
   * 广播选择事件
   * @param event
   */
  private void broadcastSelectionEvent(SelectionEvent event) {
    for (SelectionListener listener : mySelectionListeners) {
      try {
        listener.selectionChanged(event);
      }
      catch (Exception e) {
        LOG.error(e);
      }
    }
  }

  /**
   * 设置选择块
   * @param blockStart the start of the rectangle to select.  要选择的矩形的开头。
   * @param blockEnd   the end of the rectangle to select.    要选择的矩形的结尾。
   */
  @Override
  public void setBlockSelection(@NotNull LogicalPosition blockStart, @NotNull LogicalPosition blockEnd) {
    List<CaretState> caretStates = EditorModificationUtil.calcBlockSelectionState(myEditor, blockStart, blockEnd);
    myEditor.getCaretModel().setCaretsAndSelections(caretStates);
  }

  /**
   * 获取块选择开始
   * @return
   */
  @Override
  public int @NotNull [] getBlockSelectionStarts() {
    Collection<Caret> carets = myEditor.getCaretModel().getAllCarets();
    int[] result = new int[carets.size()];
    int i = 0;
    for (Caret caret : carets) {
      result[i++] = caret.getSelectionStart();
    }
    return result;
  }

  /**
   * 获取块选择结束
   * @return
   */
  @Override
  public int @NotNull [] getBlockSelectionEnds() {
    Collection<Caret> carets = myEditor.getCaretModel().getAllCarets();
    int[] result = new int[carets.size()];
    int i = 0;
    for (Caret caret : carets) {
      result[i++] = caret.getSelectionEnd();
    }
    return result;
  }

  /**
   * 增加选择监听器
   * @param listener the listener instance.
   */
  @Override
  public void addSelectionListener(@NotNull SelectionListener listener) {
    mySelectionListeners.add(listener);
  }

  /**
   * 移除选择监听器
   * @param listener the listener instance.
   */
  @Override
  public void removeSelectionListener(@NotNull SelectionListener listener) {
    boolean success = mySelectionListeners.remove(listener);
    LOG.assertTrue(success);
  }

  /**
   * 复制选择到剪贴板
   */
  @Override
  public void copySelectionToClipboard() {
    EditorCopyPasteHelper.getInstance().copySelectionToClipboard(myEditor);
  }

  /**
   * 获取文本属性
   * @return 编辑器文本的前景色和背景色。（选择器）
   */
  @Override
  public TextAttributes getTextAttributes() {
    if (myTextAttributes == null) {
      TextAttributes textAttributes = new TextAttributes();
      EditorColorsScheme scheme = myEditor.getColorsScheme();
      textAttributes.setForegroundColor(scheme.getColor(EditorColors.SELECTION_FOREGROUND_COLOR));
      textAttributes.setBackgroundColor(scheme.getColor(EditorColors.SELECTION_BACKGROUND_COLOR));
      myTextAttributes = textAttributes;
    }

    return myTextAttributes;
  }

  /**
   * 重新初始话文本设置， 文本设置置空
   */
  public void reinitSettings() {
    myTextAttributes = null;
  }
}
