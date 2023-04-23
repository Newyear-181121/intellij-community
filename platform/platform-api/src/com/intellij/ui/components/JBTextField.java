// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.ui.components;

import com.intellij.ui.TextAccessor;
import com.intellij.util.ui.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.TextUI;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;

public class JBTextField extends JTextField implements ComponentWithEmptyText, TextAccessor {
  private TextComponentEmptyText myEmptyText;

  public JBTextField() {
    init();
  }

  public JBTextField(int columns) {
    super(columns);
    init();
  }

  public JBTextField(@Nls String text) {
    super(text);
    init();
  }

  public JBTextField(@Nls String text, int columns) {
    super(text, columns);
    init();
  }

  @Override
  protected Graphics getComponentGraphics(Graphics graphics) {
    return JBSwingUtilities.runGlobalCGTransform(this, super.getComponentGraphics(graphics));
  }


  /**
   * 初始化操作，
   * 添加一个撤销重做操作，<bt/>
   * 添加一个空文本组件，<bt/>
   */
  private void init() {
    SwingUndoUtil.addUndoRedoActions(this);
    myEmptyText = new TextComponentEmptyText(this, true) {
      @Override
      protected Rectangle getTextComponentBound() {
        //获取空文本组件边界
        return getEmptyTextComponentBounds(super.getTextComponentBound());
      }
    };
  }

  /**
   *
   * @param bounds
   * @return 矩形
   */
  protected Rectangle getEmptyTextComponentBounds(Rectangle bounds) {
    return bounds;
  }

  /**
   * 设置文本以触发空文本状态
   * @param t
   */
  public void setTextToTriggerEmptyTextStatus(String t) {
    myEmptyText.setTextToTriggerStatus(t);
  }

  @Override
  public void setText(String t) {
    if (Objects.equals(t, getText())) return;
    super.setText(t);
    // 重置撤消重做操作
    SwingUndoUtil.resetUndoRedoActions(this);
  }

  /**
   * 获取空文本
   * @return
   */
  @Override
  public @NotNull StatusText getEmptyText() {
    return myEmptyText;
  }

  /**
   * 油漆组件
   * @param g
   */
  @Override
  public void updateUI() {
    super.updateUI();
    if (getParent() != null) myEmptyText.resetFont();
  }

  @Override
    public void updateUI() {
    super.updateUI();
    if (getParent() != null) myEmptyText.resetFont();
  }

  @Override
  @SuppressWarnings("DuplicatedCode")
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    //获取状态触发文本 -- 不是空，而且是可见的。
    if (!myEmptyText.getStatusTriggerText().isEmpty() && myEmptyText.isStatusVisible()) {
      // 设置绘画的颜色是背景色
      g.setColor(getBackground());

      Rectangle rect = new Rectangle(getSize());
      JBInsets.removeFrom(rect, getInsets());
      JBInsets.removeFrom(rect, getMargin());
      ((Graphics2D)g).fill(rect);

      g.setColor(getForeground());
    }

    //绘制状态文本
    myEmptyText.paintStatusText(g);
  }

  /**
   * 获取工具提示文本
   * @param event
   * @return
   */
  @Override
  public String getToolTipText(MouseEvent event) {
    TextUI ui = getUI();
    String text = ui == null ? null : ui.getToolTipText2D(this, event.getPoint());
    return text != null ? text : getToolTipText();
  }
}
