// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.diagnostic;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * 加载状态
 */
@ApiStatus.Internal
public enum LoadingState {
  BOOTSTRAP("bootstrap"),
  /**
   * 基础 LAF 已初始化
   */
  BASE_LAF_INITIALIZED("LaF is initialized"),
  /**
   * 组件已注册
   */
  COMPONENTS_REGISTERED("app component registered"),
  /**
   * 配置存储已初始化
   */
  CONFIGURATION_STORE_INITIALIZED("app store initialized"),
  /**
   * 加载的组件
   */
  COMPONENTS_LOADED("app component loaded"),
  /**
   * 应用启动
   */
  APP_STARTED("app started"),
  /**
   * 项目启动
   */
  PROJECT_OPENED("project opened");

  /**
   * 显示名称
   */
  final String displayName;

  /**
   * 错误处理程序
   */
  @SuppressWarnings("StaticNonFinalField")
  public static BiConsumer<String, Throwable> errorHandler;

  /**
   * 检查加载阶段
   */
  private static boolean CHECK_LOADING_PHASE;
  /**
   * 堆栈跟踪
   */
  private static Set<ThrowableWrapper> stackTraces;

  /**
   * 构造方法
   * @param displayName
   */
  LoadingState(@NotNull String displayName) {
    this.displayName = displayName;
  }

  /**
   * 设置严格模式
   */
  @ApiStatus.Internal
  public static void setStrictMode() {
    CHECK_LOADING_PHASE = true;
  }

  /**
   * 检查发生
   */
  public void checkOccurred() {
    if (!CHECK_LOADING_PHASE) {
      return;
    }

    LoadingState currentState = StartUpMeasurer.currentState.get();
    if (currentState.compareTo(this) >= 0 || isKnownViolator()) {
      return;
    }

    logStateError(currentState);
  }

  /**
   * 记录状态错误
   * @param currentState
   */
  private synchronized void logStateError(@NotNull LoadingState currentState) {
    Throwable t = new Throwable();
    if (stackTraces == null) {
      //noinspection AssignmentToStaticFieldFromInstanceMethod
      stackTraces = new HashSet<>();
    }

    if (!stackTraces.add(new ThrowableWrapper(t))) {
      return;
    }

    BiConsumer<String, Throwable> errorHandler = LoadingState.errorHandler;
    if (errorHandler != null) {
      errorHandler.accept("Should be called at least in the state " + this + ", the current state is: " + currentState + "\n" +
                          "Current violators count: " + stackTraces.size() + "\n\n",
                          t);
    }
  }

  private static final class ThrowableWrapper {
    final Throwable throwable;

    private ThrowableWrapper(Throwable throwable) {
      this.throwable = throwable;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }

      if (obj instanceof ThrowableWrapper) {
        Throwable throwable = ((ThrowableWrapper)obj).throwable;
        if (this.throwable == throwable || fingerprint(this.throwable).equals(fingerprint(throwable))) {
          return true;
        }
      }
      return false;
    }

    @Override
    public int hashCode() {
      return fingerprint(throwable).hashCode();
    }

    /**
     * 获取异常指纹，及堆栈记录
     * @param throwable
     * @return
     */
    private static String fingerprint(Throwable throwable) {
      StringBuilder sb = new StringBuilder();
      for (StackTraceElement traceElement : throwable.getStackTrace()) {
        sb.append(traceElement.getClassName()).append(traceElement.getMethodName());
      }
      return sb.toString();
    }
  }

  private static boolean isKnownViolator() {
    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
      String className = element.getClassName();
      if (className.contains("com.intellij.util.indexing.IndexInfrastructure")
          || className.contains("com.intellij.psi.impl.search.IndexPatternSearcher")
          || className.contains("com.jetbrains.performancePlugin.ProjectLoaded")) {
        return true;
      }
    }
    return false;
  }

  public boolean isOccurred() {
    return StartUpMeasurer.currentState.get().compareTo(this) >= 0;
  }
}
