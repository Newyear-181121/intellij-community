// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.ui.components;

import javax.swing.*;

/**
 * @author New Year
 * @since 2022/4/28 21:48
 */
public class JBTextAreaTest {

  public static void main(String[] args) {
    JBTextArea test = new JBTextArea();

    JFrame frame = new JFrame("Text Edit");

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(test);
    frame.setSize(640, 480);
    frame.setVisible(true);
  }
}
