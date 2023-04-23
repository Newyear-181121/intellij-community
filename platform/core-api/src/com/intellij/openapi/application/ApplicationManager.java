// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.application;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.DefaultLogger;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 应用程序管理器
 * Provides access to the {@link Application}.
 */
public class ApplicationManager {
  protected static Application ourApplication;

  public static Application getApplication() {
    //System.out.println("获取应用程序管理器实例开始！");
    //System.out.println(DefaultLogger.getStackTrace(false));
    return ourApplication;
  }

  @ApiStatus.Internal
  public static void setApplication(@Nullable Application instance) {
    System.out.println("设置应用程序管理器实例开始" + instance );
    System.out.println(DefaultLogger.getStackTrace(false));
    ourApplication = instance;
    // 缓存单例注册表，   清理缓存字段
    CachedSingletonsRegistry.cleanupCachedFields();
  }

  public static void setApplication(@NotNull Application instance, @NotNull Disposable parent) {
    Application old = ourApplication;
    Disposer.register(parent, () -> {
      if (old != null) { // to prevent NPEs in threads still running
        setApplication(old);
      }
    });
    setApplication(instance);
  }

  public static void setApplication(@NotNull Application instance,
                                    @NotNull Supplier<? extends FileTypeRegistry> fileTypeRegistryGetter,
                                    @NotNull Disposable parent) {
    Application old = ourApplication;
    setApplication(instance);
    Supplier<? extends FileTypeRegistry> oldFileTypeRegistry = FileTypeRegistry.setInstanceSupplier(fileTypeRegistryGetter);
    Disposer.register(parent, () -> {
      if (old != null) {
        // to prevent NPEs in threads still running
        setApplication(old);
        FileTypeRegistry.setInstanceSupplier(oldFileTypeRegistry);
      }
    });
  }
}
