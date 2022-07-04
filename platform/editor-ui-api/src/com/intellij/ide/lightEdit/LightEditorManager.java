// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ide.lightEdit;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * 高亮编辑器管理器
 */
@ApiStatus.Experimental
public interface LightEditorManager {
  void addListener(@NotNull LightEditorListener listener);

  void addListener(@NotNull LightEditorListener listener, @NotNull Disposable disposable);

  LightEditorInfo saveAs(@NotNull LightEditorInfo info, @NotNull VirtualFile targetFile);

  @NotNull LightEditorInfo createEmptyEditor(@Nullable String preferredName);

  @Nullable LightEditorInfo createEditor(@NotNull VirtualFile file);

  void closeEditor(@NotNull LightEditorInfo editorInfo);

  /**
   * 包含未保存的文档
   * @return
   */
  boolean containsUnsavedDocuments();

  /**
   * 是否允许隐式保存
   * @param document
   * @return
   */
  boolean isImplicitSaveAllowed(@NotNull Document document);

  /**
   * 获取打开的文件
   * @return
   */
  @NotNull
  Collection<VirtualFile> getOpenFiles();

  /**
   * 获取编辑器
   * @param virtualFile
   * @return
   */
  @NotNull
  Collection<LightEditorInfo> getEditors(@NotNull VirtualFile virtualFile);

  /**
   * 是文件打开
   * @param file
   * @return
   */
  boolean isFileOpen(@NotNull VirtualFile file);
}
