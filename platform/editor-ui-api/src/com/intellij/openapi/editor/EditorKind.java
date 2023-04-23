// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.editor;

/**
 * Distinguishes between:
 * <ul>
 * <li>a main editor
 * <li>a console editor, typically used to display the read-only output of a process
 * <li>a preview editor, for search results, code style or highlighting
 * <li>a diff editor, for comparing two documents
 * </ul>
 */
public enum EditorKind {
  /**
   * 未分类
   */
  UNTYPED,
  /**
   * 主编
   */
  MAIN_EDITOR,  // instead of SoftWrapAppliancePlaces.MAIN_EDITOR
  /**
   * 控制台视图
   */
  CONSOLE,      // EDITOR_IS_CONSOLE_VIEW, SoftWrapAppliancePlaces.CONSOLE
  /**
   * 预览
   */
  PREVIEW,      // SoftWrapAppliancePlaces.PREVIEW
  /**
   * 差异
   */
  DIFF         // EDITOR_IS_DIFF_KEY
}
