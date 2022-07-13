// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.editor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * 提供获取编辑器可见区域和滚动编辑器的服务。
 * Provides services for getting the visible area of the editor and scrolling the editor.
 *
 * @see Editor#getScrollingModel()
 */
public interface ScrollingModel {
  /**
   * 获取可见区域
   * @return
   */
  @NotNull
  Rectangle getVisibleArea();

  /**
   * 滚动完成时获取可见区域
   * @return
   */
  @NotNull
  Rectangle getVisibleAreaOnScrollingFinished();

  /**
   * 滚动到插入符号
   * @param scrollType
   */
  void scrollToCaret(@NotNull ScrollType scrollType);
  void scrollTo(@NotNull LogicalPosition pos, @NotNull ScrollType scrollType);

  /**
   * 滚动完成时运行操作
   * @param action
   */
  void runActionOnScrollingFinished(@NotNull Runnable action);

  /**
   * 禁用动画
   */
  void disableAnimation();

  /**
   * 启用动画
   */
  void enableAnimation();

  int getVerticalScrollOffset();
  int getHorizontalScrollOffset();

  void scrollVertically(int scrollOffset);
  void scrollHorizontally(int scrollOffset);
  void scroll(int horizontalOffset, int verticalOffset);

  void addVisibleAreaListener(@NotNull VisibleAreaListener listener);
  void removeVisibleAreaListener(@NotNull VisibleAreaListener listener);
  default void addVisibleAreaListener(@NotNull VisibleAreaListener listener, @NotNull Disposable disposable) {
    addVisibleAreaListener(listener);
    Disposer.register(disposable, () -> removeVisibleAreaListener(listener));
  }

  /**
   * 供应商
   */
  interface Supplier {
    @NotNull Editor getEditor();
    @NotNull JScrollPane getScrollPane();
    @NotNull ScrollingHelper getScrollingHelper();
  }

  /**
   * 滚动助手
   */
  interface ScrollingHelper {
    @NotNull Point calculateScrollingLocation(@NotNull Editor editor, @NotNull VisualPosition pos);
    @NotNull Point calculateScrollingLocation(@NotNull Editor editor, @NotNull LogicalPosition pos);
  }
}
