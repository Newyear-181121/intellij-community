// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.command.undo;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class BrokenUndoTest extends UndoTestCase {

  public void test_myTestViewer() {
    System.out.println(myRoot);
    VirtualFile file = createChildData(myRoot, "f.txt");
    final Document document = FileDocumentManager.getInstance().getDocument(file);
    assertNotNull(document);
    EditorFactory editorFactory = EditorFactory.getInstance();
    Editor editor = editorFactory.createViewer(document);
    if (editor instanceof EditorImpl) {
      EditorImpl impl = (EditorImpl) editor;
      JFrame jFrame = new JFrame("测试");
      jFrame.setSize(300, 300);
      jFrame.setLocation(300, 200);
      jFrame.add(impl.getScrollPane());
      jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      jFrame.setVisible(true);
    }
  }

  public static void main(String[] args) {
    BrokenUndoTest test = new BrokenUndoTest();

    test.test_myTestViewer();
  }

  //public BrokenUndoTest() {
  //  super.myRoot = createTestProjectStructure();
  //}

  public void testWithEditorViewer() {
    System.out.println(myRoot);
    VirtualFile file = createChildData(myRoot, "f.txt");
    final Document document = FileDocumentManager.getInstance().getDocument(file);
    assertNotNull(document);
    WriteCommandAction.runWriteCommandAction(getProject(), () -> document.setText("Some initial text content"));

    performInEditor(document, false, editor -> executeCommand(() ->
                                                                WriteCommandAction.runWriteCommandAction(getProject(), () -> document.setText("Some initial text modification")),
                                                              "Initial modification"));

    performInEditor(document, true, editor -> executeCommand(() ->
                                                               WriteCommandAction.runWriteCommandAction(getProject(), () -> document.setText("Some breaking modification")),
                                                             "Breaking modification"));

    performInEditor(document, false, editor -> undo(editor));

    assertEquals("Some initial text modification", document.getText());
  }

  private static void performInEditor(@NotNull Document document, boolean isViewer, Consumer<? super Editor> task) {
    EditorFactory editorFactory = EditorFactory.getInstance();
    Editor editor = isViewer ? editorFactory.createViewer(document) : editorFactory.createEditor(document);
    try {
      task.consume(editor);
    }
    finally {
      editorFactory.releaseEditor(editor);
    }
  }
}
