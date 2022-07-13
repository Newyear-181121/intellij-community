// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.editor.impl;

import com.intellij.openapi.application.impl.ApplicationImpl;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorKind;
import com.intellij.util.ui.EDT;

import java.io.File;

/**
 * @author New Year
 * @since 2022/6/30 16:01
 */
public class EditorImplDemo {

  public static void main(String[] args) {
    File file = new File("file://D:/learn/JavaProject/hutool/README.md");

    EDT.updateEdt();
    ApplicationImpl app = new ApplicationImpl(true, false, false, EDT.getEventDispatchThread());

    EditorFactoryImpl editorFactory = new EditorFactoryImpl();

    String str= "sldkfjewljfew";
    Document document = editorFactory.createDocument(str.toCharArray());



    Editor editor = editorFactory.createEditor(document, null, EditorKind.MAIN_EDITOR);

    //Document document = FileDocumentManager.getInstance().getDocument(file);
    //
    //CompletableFuture<Void> future = CompletableFuture.runAsync(
    //  () -> new EditorImpl(document, false,  , EditorKind.MAIN_EDITOR)
    //  , ForkJoinPool.commonPool());
  }
}
