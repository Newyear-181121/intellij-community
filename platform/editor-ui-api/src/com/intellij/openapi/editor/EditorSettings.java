// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.editor;

import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * 编辑器设置接口
 */
public interface EditorSettings {
  /**
   * 是否显示右边距
   * @return
   */
  boolean isRightMarginShown();
  void setRightMarginShown(boolean val);

  /**
   * 是否显示空格
   * @return
   */
  boolean isWhitespacesShown();
  void setWhitespacesShown(boolean val);

  /**
   * 是否显示前导空白
   * @return
   */
  boolean isLeadingWhitespaceShown();
  void setLeadingWhitespaceShown(boolean val);

  /**
   * 是否显示内部空白
   * @return
   */
  boolean isInnerWhitespaceShown();
  void setInnerWhitespaceShown(boolean val);

  /**
   * 是否显示尾随空格
   * @return
   */
  boolean isTrailingWhitespaceShown();
  void setTrailingWhitespaceShown(boolean val);

  boolean isSelectionWhitespaceShown();
  void setSelectionWhitespaceShown(boolean val);

  /**
   * 获取右边距
   * @param project
   * @return
   */
  int getRightMargin(Project project);
  void setRightMargin(int myRightMargin);

  /**
   * 获取软边距
   * <br/>
   * 检索要在编辑器中使用的软边距（可视缩进参考线）列表。如果软边距尚未使用 setSoftMargins（List） 方法显式设置，则从代码样式设置中获取软边距：CodeStyleSettings.getSoftMargins（）。
   * <br/>
   * Retrieves a list of soft margins (visual indent guides) to be used in the editor. If soft margins haven't been explicitly set
   * with {@link #setSoftMargins(List)} method, they are obtained from code style settings: {@code CodeStyleSettings.getSoftMargins()}.
   * @return A list of current editor soft margins. The list may be empty if no soft margins are defined. <br/> 当前编辑器软边距的列表。如果未定义软边距，则列表可能为空。
   */
  @NotNull
  List<Integer> getSoftMargins();

  /**
   * 设置软边距列表
   * <br/>
   * Explicitly sets soft margins (visual indent guides) to be used in the editor instead of obtaining them from code style settings via
   * {@code CodeStyleSettings.getSoftMargins()} method. It is important to distinguish and empty list from {@code null} value: the first
   * will define no soft margins for the editor while the latter will restore the default behavior of using them from code style settings.
   * @param softMargins A list of soft margins or {@code null} to use margins from code style settings.
   */
  void setSoftMargins(@Nullable List<Integer> softMargins);

  /**
   * 当打字到达右边距时 是否换行
   * @param project
   * @return
   */
  boolean isWrapWhenTypingReachesRightMargin(Project project);
  void setWrapWhenTypingReachesRightMargin(boolean val);

  /**
   * 是否显示行号
   * @return
   */
  boolean isLineNumbersShown();
  void setLineNumbersShown(boolean val);

  /**
   * 获取其他行数
   * @return
   */
  int getAdditionalLinesCount();
  void setAdditionalLinesCount(int additionalLinesCount);

  /**
   * 获取其他列计数
   * @return
   */
  int getAdditionalColumnsCount();
  void setAdditionalColumnsCount(int additionalColumnsCount);

  /**
   * 是否显示的线标记区域
   * @return
   */
  boolean isLineMarkerAreaShown();
  void setLineMarkerAreaShown(boolean lineMarkerAreaShown);

  /**
   * 是否显示 显示装订线图标  （行号旁边的图标）
   * @return
   */
  boolean areGutterIconsShown();
  void setGutterIconsShown(boolean gutterIconsShown);

  /**
   * 是否显示折叠轮廓
   * @return
   */
  boolean isFoldingOutlineShown();
  void setFoldingOutlineShown(boolean val);

  /**
   * 是否已启用自动代码折叠
   * @return
   */
  boolean isAutoCodeFoldingEnabled();
  void setAutoCodeFoldingEnabled(boolean val);

  /**
   * 是否使用制表符
   * @param project
   * @return
   */
  boolean isUseTabCharacter(Project project);
  void setUseTabCharacter(boolean useTabCharacter);

  /**
   * 获取选项卡大小
   * @param project
   * @return
   */
  int getTabSize(Project project);
  void setTabSize(int tabSize);

  /**
   * 是否是是 smartHome   smartHome 是指什么
   * @return
   */
  boolean isSmartHome();
  void setSmartHome(boolean val);

  /**
   * 是否是虚拟空间
   * @return
   */
  boolean isVirtualSpace();
  void setVirtualSpace(boolean allow);

  /**
   * 是否是插入符号内部标签
   * @return
   */
  boolean isCaretInsideTabs();
  void setCaretInsideTabs(boolean allow);

  /**
   * 是否闪烁 插入标记
   * @return
   */
  boolean isBlinkCaret();
  void setBlinkCaret(boolean blinkCaret);

  /**
   * 获取光标闪烁周期
   * @return
   */
  int getCaretBlinkPeriod();
  void setCaretBlinkPeriod(int blinkPeriod);

  /**
   * 是否是块光标
   * @return
   */
  boolean isBlockCursor();
  void setBlockCursor(boolean blockCursor);

  /**
   * 是否显示插入符号行
   * @return
   */
  boolean isCaretRowShown();
  void setCaretRowShown(boolean caretRowShown);

  /**
   * 获取行光标宽度
   * @return
   */
  int getLineCursorWidth();
  void setLineCursorWidth(int width);

  /**
   * 是否是动画滚动
   * @return
   */
  boolean isAnimatedScrolling();
  void setAnimatedScrolling(boolean val);

  /**
   * 是否是驼峰词
   * @return
   */
  boolean isCamelWords();
  void setCamelWords(boolean val);
  /**
   * 允许删除特定于当前设置对象（如果有）的“使用骆驼词”设置并使用共享的设置。
   * <br/>
   * Allows to remove 'use camel words' setup specific to the current settings object (if any) and use the shared one.
   */
  void resetCamelWords();

  /**
   * 是否是底部的附加页
   */
  boolean isAdditionalPageAtBottom();
  void setAdditionalPageAtBottom(boolean val);

  boolean isDndEnabled();
  void setDndEnabled(boolean val);

  boolean isWheelFontChangeEnabled();
  void setWheelFontChangeEnabled(boolean val);

  boolean isMouseClickSelectionHonorsCamelWords();
  void setMouseClickSelectionHonorsCamelWords(boolean val);


  boolean isVariableInplaceRenameEnabled();
  void setVariableInplaceRenameEnabled(boolean val);

  boolean isRefrainFromScrolling();
  void setRefrainFromScrolling(boolean b);

  boolean isIndentGuidesShown();
  void setIndentGuidesShown(boolean val);

  boolean isUseSoftWraps();
  void setUseSoftWraps(boolean use);
  boolean isAllSoftWrapsShown();

  default boolean isPaintSoftWraps() {
    return true;
  }
  default void setPaintSoftWraps(boolean val) {}

  boolean isUseCustomSoftWrapIndent();
  void setUseCustomSoftWrapIndent(boolean useCustomSoftWrapIndent);
  int getCustomSoftWrapIndent();
  void setCustomSoftWrapIndent(int indent);

  /**
   * @see #setAllowSingleLogicalLineFolding(boolean)
   */
  boolean isAllowSingleLogicalLineFolding();

  /**
   * By default, gutter mark (for collapsing/expanding the region using mouse) is not shown for a folding region, if it's contained within
   * a single document line. If overridden by the call to this method, marks will be displayed for such a region if it occupies multiple
   * visual lines (due to soft wrapping). Displaying a gutter mark can be also enabled for a region unconditionally using
   * {@link FoldRegion#setGutterMarkEnabledForSingleLine(boolean)}.
   */
  void setAllowSingleLogicalLineFolding(boolean allow);

  boolean isPreselectRename();
  void setPreselectRename(final boolean val);

  boolean isShowIntentionBulb();
  void setShowIntentionBulb(boolean show);

  /**
   * Sets the language which determines certain editor settings (right margin and soft margins, 'wrap on reaching right margin').
   *
   * @see #getRightMargin(Project)
   * @see #getSoftMargins()
   * @see #isWrapWhenTypingReachesRightMargin(Project)
   */
  void setLanguageSupplier(@Nullable Supplier<? extends Language> languageSupplier);

  boolean isShowingSpecialChars();
  void setShowingSpecialChars(boolean value);

  /**
   * @deprecated This method is a stub. Related functionality has been moved to {@code VisualFormattingLayerService}.
   */
  @Deprecated
  default @Nullable Boolean isShowVisualFormattingLayer() { return null; }

  /**
   * @deprecated This method is a stub. Related functionality has been moved to {@code VisualFormattingLayerService}.
   */
  @Deprecated
  default void setShowVisualFormattingLayer(@Nullable Boolean showVisualFormattingLayer) {}

  boolean isInsertParenthesesAutomatically();
}
