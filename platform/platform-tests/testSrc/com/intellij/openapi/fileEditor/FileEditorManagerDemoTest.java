// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.fileEditor;

import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;

/**
 * @author New Year
 * @since 2022/5/6 9:00
 */
public class FileEditorManagerDemoTest extends FileEditorManagerTest{

  public void test() {
    //FileEditorProvider.EP_FILE_EDITOR_PROVIDER.getPoint().registerExtension(new FileEditorManagerTest.MyFileEditorProvider(), myFixture.getTestRootDisposable());
    VirtualFile file = getFile("/src/1.txt");
    assertNotNull(file);
    FileEditor[] editors = myManager.openFile(file, true);

    JFrame frame = new JFrame();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(editors[0].getComponent());
    frame.setSize(640, 480);
    frame.setVisible(true);
  }

  public static void main(String[] args) {
    FileEditorManagerDemoTest test = new FileEditorManagerDemoTest();
    test.test();
  }
}
