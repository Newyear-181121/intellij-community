/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.editor;

/**
 * Enumerates distinct types of editor.
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
