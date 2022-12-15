// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.editor.impl;

import com.intellij.openapi.editor.*;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

/**
 * 鼠标位置
 * <br/>
 * 这里的位置信息都是通过编辑器获取的， 也可以说是对编辑器内的位置信息包装一层。
 */
class EditorLocation {
  private final Editor myEditor;
  private final Point myPoint;
  /**
   * 视觉位置
   */
  private VisualPosition myVisualPosition;
  /**
   * 逻辑位置
   */
  private LogicalPosition myLogicalPosition;
  private int myOffset = -1;
  /**
   * 视觉行范围
   */
  private int[] myVisualLineYRange;
  /**
   * 折叠区域
   */
  private FoldRegion myCollapsedRegion = NO_REGION;

  EditorLocation(@NotNull Editor editor, @NotNull Point point) {
    myEditor = editor;
    myPoint = point;
  }

  @NotNull Point getPoint() {
    return myPoint;
  }

  /**
   * 获取视觉位置
   * @return
   */
  @NotNull VisualPosition getVisualPosition() {
    if (myVisualPosition == null) {
      myVisualPosition = myEditor.xyToVisualPosition(myPoint);
    }
    return myVisualPosition;
  }

  /**
   * 获取视觉行开始位置
   * @return
   */
  int getVisualLineStartY() {
    if (myVisualLineYRange == null) {
      myVisualLineYRange = myEditor.visualLineToYRange(getVisualPosition().line);
    }
    return myVisualLineYRange[0];
  }

  /**
   * 获取视觉行结束位置
   * @return
   */
  int getVisualLineEndY() {
    if (myVisualLineYRange == null) {
      myVisualLineYRange = myEditor.visualLineToYRange(getVisualPosition().line);
    }
    return myVisualLineYRange[1];
  }

  /**
   * 获取逻辑位置
   * @return
   */
  @NotNull LogicalPosition getLogicalPosition() {
    if (myLogicalPosition == null) {
      myLogicalPosition = myEditor.visualToLogicalPosition(getVisualPosition());
    }
    return myLogicalPosition;
  }

  /**
   * 获取偏移量
   * @return
   */
  int getOffset() {
    if (myOffset < 0) {
      myOffset = myEditor.logicalPositionToOffset(getLogicalPosition());
    }
    return myOffset;
  }

  /**
   * 获取折叠区域
   * @return
   */
  FoldRegion getCollapsedRegion() {
    if (myCollapsedRegion == NO_REGION) {
      myCollapsedRegion = myEditor.getFoldingModel().getCollapsedRegionAtOffset(getOffset());
    }
    return myCollapsedRegion;
  }

  private static final FoldRegion NO_REGION = new FoldRegion() {
    @Override
    public boolean isExpanded() {
      return false;
    }

    @Override
    public void setExpanded(boolean expanded) {}

    @Override
    public @NotNull String getPlaceholderText() {
      return "";
    }

    @Override
    public Editor getEditor() {
      return null;
    }

    @Override
    public @Nullable FoldingGroup getGroup() {
      return null;
    }

    @Override
    public boolean shouldNeverExpand() {
      return false;
    }

    @Override
    public @NotNull Document getDocument() {
      throw new UnsupportedOperationException();
    }

    @Override
    public int getStartOffset() {
      return 0;
    }

    @Override
    public int getEndOffset() {
      return 0;
    }

    @Override
    public boolean isValid() {
      return false;
    }

    @Override
    public void setGreedyToLeft(boolean greedy) {}

    @Override
    public void setGreedyToRight(boolean greedy) {}

    @Override
    public boolean isGreedyToRight() {
      return false;
    }

    @Override
    public boolean isGreedyToLeft() {
      return false;
    }

    @Override
    public void dispose() {}

    @Override
    public <T> @Nullable T getUserData(@NotNull Key<T> key) {
      return null;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {}
  };
}
