// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.ui.components.editors;

import javax.swing.*;

/**
 * @author New Year
 * @since 2022/5/5 14:54
 */
public class JBComboBoxTableCellTest {
  public static void main(String[] args) {

    JBComboBoxTableCellEditorComponent test = new JBComboBoxTableCellEditorComponent();
    test.setTable(new JTable());

    JFrame frame = new JFrame("Text Edit");

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(test);
    frame.setSize(640, 480);
    frame.setVisible(true);
  }
}
