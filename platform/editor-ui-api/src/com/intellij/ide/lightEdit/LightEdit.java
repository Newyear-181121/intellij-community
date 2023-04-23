// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.ide.lightEdit;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 亮度编辑
 * 一个围绕 {@code LightEditService} 方法的包装类。
 * A wrapping class around {@link LightEditService} methods.
 */
public final class LightEdit {
  private LightEdit() {
  }

  @Contract("null -> false")
  public static boolean owns(@Nullable Project project) {
    return project instanceof LightEditCompatible;
  }

  public static boolean isActionCompatible(@NotNull AnAction action) {
    return (action instanceof ActionGroup) && action.isDumbAware()
           || action instanceof LightEditCompatible;
  }

}
