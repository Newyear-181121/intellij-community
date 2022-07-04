// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.ide.lightEdit;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

/**
 * 高亮编辑器信息
 */
@ApiStatus.Experimental
public interface LightEditorInfo {

  @NotNull FileEditor getFileEditor();

  @NotNull VirtualFile getFile();

  /**
   * 如果文档是新的且从未保存过或已修改但未保存，则为真。
   * @return True if the document either is new and has never been saved or has been modified but not saved.
   * @see #isSaveRequired()
   */
  boolean isUnsaved();

  /**
   * 如果文档仅存在于内存中并且没有相应的物理文件，则为真
   * @return True if the document exists only in memory and doesn't have a corresponding physical file.
   */
  boolean isNew();

  /**
   * 对于已保存但已修改的文档，与 {@link isUnsaved()} 的值相同。对于尚未保存的新文档（仅存在于内存中），仅当文档内容不为空时才返回 true。
   * @return The same value as {@link #isUnsaved()} for already saved but modified documents. For new documents which have never been
   * saved yet (exist only in memory), returns true only if document content is not empty.
   */
  boolean isSaveRequired();

  @Nullable Path getPreferredSavePath();

  void setPreferredSavePath(@Nullable Path preferredSavePath);
}
