// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeHighlighting;

import org.jetbrains.annotations.NotNull;

/**
 * 背景编辑器高亮
 */
public interface BackgroundEditorHighlighter {
  /**
   * 为编辑器创建通行证
   * @return
   */
  HighlightingPass @NotNull [] createPassesForEditor();
}
