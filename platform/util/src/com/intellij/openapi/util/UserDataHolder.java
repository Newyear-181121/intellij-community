// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 用户数据持有者
 * 允许在模型对象中存储自定义用户数据。
 * 这可能比具有模型对象作为键和值中的自定义数据的显式 Map 更可取，因为这允许数据与值一起被垃圾收集。
 * Allows storing custom user data within a model object. This might be preferred to an explicit Map with model objects as keys and
 * custom data in values because this allows the data to be garbage-collected together with the values.
 * <p>
 * If you need to implement this interface, extend {@link UserDataHolderBase} or delegate to its instance instead of writing your own implementation.
 * 如果您需要实现此接口，请扩展 {@link UserDataHolderBase} 或委托给它的实例，而不是编写您自己的实现。
 */
public interface UserDataHolder {
  /**
   * @return a user data value associated with this object. Doesn't require read action.
   */
  @Nullable <T> T getUserData(@NotNull Key<T> key);

  /**
   * Add a new user data value to this object. Doesn't require write action.
   */
  <T> void putUserData(@NotNull Key<T> key, @Nullable T value);
}