// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.ui.components;

import com.intellij.util.BooleanFunction;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ui.JBInsets;
import com.intellij.util.ui.StatusText;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.function.Predicate;

/**
 * 文本组件空文本   继承 状态文本
 */
public class TextComponentEmptyText extends StatusText {
  /**
   * Expecting an instance of {@link Predicate}&lt;{@link JTextComponent}&gt;.
   */
  // 状态可见功能
  public static final String STATUS_VISIBLE_FUNCTION = "StatusVisibleFunction";

  /**
   * 文本的基础组件
   */
  private final JTextComponent myOwner;
  private final boolean myDynamicStatus;
  /**
   * 状态触发文本， todo：还不知道是做什么用的。
   */
  private String myStatusTriggerText = "";

  TextComponentEmptyText(JTextComponent owner, boolean dynamicStatus) {
    super(owner);
    myOwner = owner;
    myDynamicStatus = dynamicStatus;
    clear();
    // 添加焦点监听器
    myOwner.addFocusListener(new FocusListener() {
      /**
       * 获得焦点
       * @param e 监听器
       */
      @Override
      public void focusGained(FocusEvent e) {
        // 重绘
        myOwner.repaint();
      }

      /**
       * 焦点丢失
       * @param e
       */
      @Override
      public void focusLost(FocusEvent e) {
        // 重绘
        myOwner.repaint();
      }
    });
  }

  /**
   * 设置文本以触发状态
   * @param defaultText
   */
  public void setTextToTriggerStatus(@NotNull String defaultText) {
    myStatusTriggerText = defaultText;
  }

  /**
   * 获取状态触发文本
   * @return
   */
  public @NotNull String getStatusTriggerText() {
    return myStatusTriggerText;
  }

  /**
   * 绘制状态文本
   * @param g
   */
  public void paintStatusText(Graphics g) {
    if (!isFontSet()) {
      setFont(myOwner.getFont());
    }
    paint(myOwner, g);
  }

  public void resetFontToOwnerFont() {
    setFont(myOwner.getFont());
  }

  /**
   * 状态是可见的
   * @return true-是，false-否
   */
  @Override
  @SuppressWarnings({"deprecation", "unchecked"})
  protected boolean isStatusVisible() {
    if (myDynamicStatus) {
      Object function = myOwner.getClientProperty(STATUS_VISIBLE_FUNCTION);
      if (function instanceof Predicate) {
        return ((Predicate<JTextComponent>)function).test(myOwner);
      }
      if (function instanceof BooleanFunction) {
        return ((BooleanFunction<JTextComponent>)function).fun(myOwner);
      }
    }

    //                                                        不是焦点的所有者，返回-false
    return myOwner.getText().equals(myStatusTriggerText) && !myOwner.isFocusOwner();
  }

  /**
   * 获取文本组件的绑定
   * @return
   */
  @Override
  protected Rectangle getTextComponentBound() {
    Rectangle b = myOwner.getBounds();
    // 插图（组件的绘画，显示页面）
    Insets insets = ObjectUtils.notNull(myOwner.getInsets(), JBInsets.emptyInsets());
    // 边，余量 （部组件内的余量）
    Insets margin = ObjectUtils.notNull(myOwner.getMargin(), JBInsets.emptyInsets());
    // 平面，平板 （内部组件的）
    Insets ipad = getComponent().getIpad();
    int left = insets.left + margin.left - ipad.left;
    int right = insets.right + margin.right - ipad.right;
    int top = insets.top + margin.top - ipad.top;
    int bottom = insets.bottom + margin.bottom - ipad.bottom;
    return new Rectangle(left, top,
                         b.width - left - right,
                         b.height - top - bottom);
  }

  /**
   * 调整组件边界
   * @param component
   * @param bounds
   * @return
   */
  @Override
  protected @NotNull Rectangle adjustComponentBounds(@NotNull JComponent component, @NotNull Rectangle bounds) {
    Dimension size = component.getPreferredSize();
    int width = Math.min(size.width, bounds.width);

    return component == getComponent()
           ? new Rectangle(bounds.x, bounds.y, width, bounds.height)
           : new Rectangle(bounds.x + bounds.width - width, bounds.y, width, bounds.height);
  }
}
