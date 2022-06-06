// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.actions

import com.intellij.codeInsight.actions.ReaderModeProvider.ReaderMode
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

/**
 * 阅读器模式匹配器
 * 使用它来覆盖文件 <--> 阅读器模式匹配
 * Use it to override file <--> reader mode matching
 */
interface ReaderModeMatcher {
  /**
   * 匹配
   * 它在阅读器模式下触发以检查文件是否与指定的模式匹配
   * It's triggered on Reader Mode to check if file matches mode specified.
   *
   * @return null if unable to decide
   */
  fun matches(project: Project, file: VirtualFile, editor: Editor?, mode: ReaderMode): Boolean?
}