// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.designer.propertyTable.editors;

import javax.swing.*;

/**
 * @author New Year
 * @since 2022/5/5 13:37
 */
public class TextEditorTest {
  public static void main(String[] args) {
    TextEditor text = new TextEditor();

    JFrame frame = new JFrame("Text Edit");

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(text.getPreferredFocusedComponent());
    frame.setSize(640, 480);
    frame.setVisible(true);
  }
}
