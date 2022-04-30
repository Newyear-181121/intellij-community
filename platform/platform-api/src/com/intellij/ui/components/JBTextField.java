/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.ui.components;

import com.intellij.ui.TextAccessor;
import com.intellij.util.BooleanFunction;
import com.intellij.util.ui.ComponentWithEmptyText;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.StatusText;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.TextUI;
import javax.swing.text.JTextComponent;
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

  /**
   * 初始化操作，
   * 添加一个撤销重做操作，<bt/>
   * 添加一个空文本组件，<bt/>
   */
  private void init() {
    // 添加撤消重做操作
    UIUtil.addUndoRedoActions(this);
    //文本组件空文本
    myEmptyText = new TextComponentEmptyText(this) {
      /**
       * 状态可见
       * @return
       */
      @Override
      protected boolean isStatusVisible() {
        //获取客户端属性
        Object function = getClientProperty(STATUS_VISIBLE_FUNCTION);
        if (function instanceof BooleanFunction) {
          //noinspection unchecked 未经检查
          return ((BooleanFunction<JTextComponent>)function).fun(JBTextField.this);
        }
        return super.isStatusVisible();
      }

      /**
       * 获取文本组件绑定
       * @return
       */
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
    UIUtil.resetUndoRedoActions(this);
  }

  /**
   * 获取空文本
   * @return
   */
  @NotNull
  @Override
  public StatusText getEmptyText() {
    return myEmptyText;
  }

  /**
   * 油漆组件
   * @param g
   */
  @Override
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
    @SuppressWarnings("HardCodedStringLiteral") String text = ui == null ? null : ui.getToolTipText(this, event.getPoint());
    return text != null ? text : getToolTipText();
  }
}
