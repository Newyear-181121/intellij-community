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
package org.jetbrains.uast

import com.intellij.psi.PsiComment
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.uast.internal.log

open class UComment(override val sourcePsi: PsiComment, private val givenParent: UElement?) : UElement {

  @Suppress("OverridingDeprecatedMember")
  @get:ApiStatus.ScheduledForRemoval
  @get:Deprecated("see the base property description")
  @Deprecated("see the base property description", ReplaceWith("sourcePsi"))
  override val psi: PsiComment get() = sourcePsi

  override val uastParent: UElement? by lazy {
    givenParent ?: sourcePsi.parent?.toUElement()
  }

  val text: String
    get() = asSourceString()

  override fun asLogString(): String = log()

  override fun asRenderString(): String = asSourceString()
  override fun asSourceString(): String = sourcePsi.text
}