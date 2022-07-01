// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.editor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.colors.EditorFontType;
import com.intellij.openapi.editor.event.EditorMouseEventArea;
import com.intellij.openapi.editor.event.EditorMouseListener;
import com.intellij.openapi.editor.event.EditorMouseMotionListener;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

/**
 * 编辑器
 * 表示文本编辑器的一个实例。
 * Represents an instance of a text editor.
 *
 * @see EditorFactory#createEditor(Document)
 * @see EditorFactory#createViewer(Document)
 */
public interface Editor extends UserDataHolder {
  Editor[] EMPTY_ARRAY = new Editor[0];

  /**
   * 返回在编辑器中编辑或查看的文档。
   * Returns the document edited or viewed in the editor.
   *
   * @return the document instance.  文档实例。
   */
  @NotNull Document getDocument();

  /**
   * 编辑器是否是查看器
   * <br/>
   * 返回指示编辑器是否在查看器模式下运行的值，所有修改操作都被禁用。
   * <br/>
   * Returns the value indicating whether the editor operates in viewer mode, with
   * all modification actions disabled.
   *
   * @return {@code true} if the editor works as a viewer, {@code false} otherwise. 如果编辑器作为查看器工作，则 {@code false} 否则。
   */
  boolean isViewer();

  /**
   * 返回整个编辑器的组件，包括滚动条、错误条、装订线和其他装饰。
   * 例如，该组件可用于将逻辑坐标转换为屏幕坐标。
   * <br/>
   * Returns the component for the entire editor including the scrollbars, error stripe, gutter
   * and other decorations. The component can be used, for example, for converting logical to
   * screen coordinates.
   *
   * @return the component instance.
   */
  @NotNull JComponent getComponent();

  /**
   * 返回编辑器内容区域的组件（显示文档文本的区域）。
   * 例如，该组件可用于将逻辑坐标转换为屏幕坐标。该实例正在实现 {@link DataProvider}。
   * <br/>
   * Returns the component for the content area of the editor (the area displaying the document text).
   * The component can be used, for example, for converting logical to screen coordinates.
   * The instance is implementing {@link DataProvider}.
   *
   * @return the component instance.
   */
  @NotNull JComponent getContentComponent();

  void setBorder(@Nullable Border border);

  Insets getInsets();

  /**
   * 返回编辑器的选择模型，可用于选择文档中的文本范围并检索有关选择的信息。
   * <br/>
   * Returns the selection model for the editor, which can be used to select ranges of text in
   * the document and retrieve information about the selection.
   * <p>
   *   要查询或更改特定插入符号的选择，应使用 {@link CaretModel} 接口。
   * To query or change selections for specific carets, {@link CaretModel} interface should be used.
   *
   * @return the selection model instance.
   * @see #getCaretModel()
   */
  @NotNull SelectionModel getSelectionModel();

  /**
   * 获取标记模型
   * <br/>
   * 返回编辑器的标记模型。
   * 此模型包含特定于编辑器的荧光笔（例如，由“文件中的突出显示用法”添加的荧光笔），这些荧光笔是在文档标记模型中包含的荧光笔之外绘制的。
   * Returns the markup model for the editor. This model contains editor-specific highlighters
   * (for example, highlighters added by "Highlight usages in file"), which are painted in addition
   * to the highlighters contained in the markup model for the document.
   * <p>
   * See also {@link com.intellij.openapi.editor.impl.DocumentMarkupModel#forDocument(Document, Project, boolean)}
   * {@link com.intellij.openapi.editor.ex.EditorEx#getFilteredDocumentMarkupModel()}.
   *
   * @return the markup model instance.  标记模型实例。
   */
  @NotNull MarkupModel getMarkupModel();

  /**
   * 获取折叠模型
   * <br/>
   *
   * 返回文档的折叠模型，可用于添加、删除、展开或折叠文档中的折叠区域。
   * Returns the folding model for the document, which can be used to add, remove, expand
   * or collapse folded regions in the document.
   *
   * @return the folding model instance.
   */
  @NotNull FoldingModel getFoldingModel();

  /**
   * 获取滚动模型
   * <br/>
   * 返回文档的滚动模型，可用于滚动文档并检索有关滚动条当前位置的信息。
   * Returns the scrolling model for the document, which can be used to scroll the document
   * and retrieve information about the current position of the scrollbars.
   *
   * @return the scrolling model instance.
   */
  @NotNull ScrollingModel getScrollingModel();

  /**
   * 获取插入符号模型
   * <br/>
   * 返回文档的插入符号模型，可用于向编辑器添加和删除插入符号，以及查询和更新插入符号和相应选择的位置。
   * Returns the caret model for the document, which can be used to add and remove carets to the editor, as well as to query and update
   * carets' and corresponding selections' positions.
   *
   * @return the caret model instance.
   */
  @NotNull CaretModel getCaretModel();

  /**
   * 获取软包装模型
   * <br/>
   * 返回文档的软包装模型，可用于获取当前为编辑器文档注册的软包装信息，并为其提供基本的管理功能。
   * Returns the soft wrap model for the document, which can be used to get information about soft wraps registered
   * for the editor document at the moment and provides basic management functions for them.
   *
   * @return the soft wrap model instance.
   */
  @NotNull SoftWrapModel getSoftWrapModel();

  /**
   * 获取设置
   * <br/>
   * 返回此编辑器实例的编辑器设置。对这些设置的更改仅影响当前编辑器实例。
   * Returns the editor settings for this editor instance. Changes to these settings affect
   * only the current editor instance.
   *
   * @return the settings instance.
   */
  @NotNull EditorSettings getSettings();

  /**
   * 获取配色方案
   * <br/>
   * 返回此编辑器实例的编辑器配色方案。对方案的更改仅影响当前编辑器实例。
   * Returns the editor color scheme for this editor instance. Changes to the scheme affect
   * only the current editor instance.
   *
   * @return the color scheme instance.
   */
  @NotNull EditorColorsScheme getColorsScheme();

  /**
   * 获取行高
   * <br/>
   * 返回当前编辑器字体中单行文本的高度
   * Returns the height of a single line of text in the current editor font.
   *
   * @return the line height in pixels.
   */
  int getLineHeight();

  /**
   * 逻辑位置到 XY
   * <br/>
   * 将编辑器中的逻辑位置映射到像素坐标
   * Maps a logical position in the editor to pixel coordinates.
   *
   * @param pos the logical position.  逻辑立场
   * @return the coordinates relative to the top left corner of the {@link #getContentComponent() content component}.相对于 {@link getContentComponent() 内容组件} 的左上角的坐标。
   */
  @NotNull Point logicalPositionToXY(@NotNull LogicalPosition pos);

  /**
   * 逻辑位置到偏移
   * <br/>
   * 将编辑器中的逻辑位置映射到文档中的偏移量。
   * Maps a logical position in the editor to the offset in the document.
   *
   * @param pos the logical position.
   * @return the corresponding offset in the document.  文档中的相应偏移量。
   */
  int logicalPositionToOffset(@NotNull LogicalPosition pos);

  /**
   * 逻辑到视觉位置
   * <br/>
   * 将编辑器中的逻辑位置（忽略折叠的行和列）映射到可视位置（折叠的行和列不包括在行和列计数中）。
   * Maps a logical position in the editor (the line and column ignoring folding) to
   * a visual position (with folded lines and columns not included in the line and column count).
   *
   * @param logicalPos the logical position.
   * @return the corresponding visual position.  对应的视觉位置
   */
  @NotNull VisualPosition logicalToVisualPosition(@NotNull LogicalPosition logicalPos);

  /**
   * 到 XY 的视觉位置
   * <br/>
   * 将编辑器中的视觉位置映射到像素坐标。
   * Maps a visual position in the editor to pixel coordinates.
   *
   * @param visible the visual position.
   * @return the coordinates relative to the top left corner of the {@link #getContentComponent() content component}.
   */
  @NotNull Point visualPositionToXY(@NotNull VisualPosition visible);

  /**
   * 视觉位置到点 2 D
   * <br/>
   * 与 {@link visualPositionToXY(VisualPosition)} 相同，但可能返回更精确的结果。
   * Same as {@link #visualPositionToXY(VisualPosition)}, but returns potentially more precise result.
   */
  @NotNull Point2D visualPositionToPoint2D(@NotNull VisualPosition pos);

  /**
   * 视觉到逻辑位置
   * <br/>
   * 将编辑器中的视觉位置（折叠的行和列不包括在行和列计数中）映射到逻辑位置（忽略折叠的行和列）。
   * Maps a visual position in the editor (with folded lines and columns not included in the line and column count) to
   * a logical position (the line and column ignoring folding).
   *
   * @param visiblePos the visual position.
   * @return the corresponding logical position.
   */
  @NotNull LogicalPosition visualToLogicalPosition(@NotNull VisualPosition visiblePos);

  /**
   * 视觉偏移位置
   * @param pos
   * @return
   */
  default int visualPositionToOffset(@NotNull VisualPosition pos) {
    return logicalPositionToOffset(visualToLogicalPosition(pos));
  }

  /**
   * 偏移到逻辑位置
   * <br/>
   * 将文档中的偏移量映射到逻辑位置。
   * <p>
   *   假设原始位置与给定偏移量之前的字符相关联，因此目标逻辑位置将 {@link LogicalPositionleansForward leansForward} 值设置为 {@code false}。
   * Maps an offset in the document to a logical position.
   * <p>
   * It's assumed that original position is associated with character immediately preceding given offset, so target logical position will
   * have {@link LogicalPosition#leansForward leansForward} value set to {@code false}.
   *
   * @param offset the offset in the document.
   * @return the corresponding logical position.
   */
  @NotNull LogicalPosition offsetToLogicalPosition(int offset);

  /**
   * Maps an offset in the document to visual position.
   * <p>
   * It's assumed that original position is associated with the character immediately preceding given offset,
   * {@link VisualPosition#leansRight leansRight} value for visual position will be determined correspondingly.
   * <p>
   * If there's a soft wrap at the given offset, visual position on a line following the wrap will be returned.
   *
   * @param offset the offset in the document.
   * @return the corresponding visual position.
   */
  @NotNull VisualPosition offsetToVisualPosition(int offset);

  /**
   * Maps an offset in the document to visual position.
   *
   * @param offset         the offset in the document.
   * @param leanForward    if {@code true}, original position is associated with character after given offset, if {@code false} -
   *                       with character before given offset. This can make a difference in bidirectional text (see {@link LogicalPosition},
   *                       {@link VisualPosition})
   * @param beforeSoftWrap if {@code true}, visual position at line preceeding the wrap will be returned, otherwise - visual position
   *                       at line following the wrap.
   * @return the corresponding visual position.
   */
  @NotNull VisualPosition offsetToVisualPosition(int offset, boolean leanForward, boolean beforeSoftWrap);

  /**
   * Maps an offset in the document to a visual line in editor.
   *
   * @param offset         the offset in the document.
   * @param beforeSoftWrap flag to resolve the ambiguity, if there's a soft wrap at target offset. If {@code true}, visual line ending in
   *                       soft wrap will be returned, otherwise - visual line following the wrap.
   * @return the visual line.
   */
  default int offsetToVisualLine(int offset, boolean beforeSoftWrap) {
    return offsetToVisualPosition(offset, false /* doesn't matter if only visual line is needed */, beforeSoftWrap).line;
  }

  /**
   * Maps the pixel coordinates in the editor to a logical position.
   *
   * @param p the coordinates relative to the top left corner of the {@link #getContentComponent() content component}.
   * @return the corresponding logical position.
   */
  @NotNull LogicalPosition xyToLogicalPosition(@NotNull Point p);

  /**
   * Maps the pixel coordinates in the editor to a visual position.
   *
   * @param p the coordinates relative to the top left corner of the {@link #getContentComponent() content component}.
   * @return the corresponding visual position.
   */
  @NotNull VisualPosition xyToVisualPosition(@NotNull Point p);

  /**
   * Same as {{@link #xyToVisualPosition(Point)}}, but allows specifying target point with higher precision.
   */
  @NotNull VisualPosition xyToVisualPosition(@NotNull Point2D p);

  default @NotNull Point offsetToXY(int offset) {
    return offsetToXY(offset, false, false);
  }

  /**
   * @see #offsetToVisualPosition(int, boolean, boolean)
   */
  default @NotNull Point offsetToXY(int offset, boolean leanForward, boolean beforeSoftWrap) {
    VisualPosition visualPosition = offsetToVisualPosition(offset, leanForward, beforeSoftWrap);
    return visualPositionToXY(visualPosition);
  }

  default @NotNull Point2D offsetToPoint2D(int offset) {
    return offsetToPoint2D(offset, false, false);
  }

  /**
   * @see #offsetToVisualPosition(int, boolean, boolean)
   */
  default @NotNull Point2D offsetToPoint2D(int offset, boolean leanForward, boolean beforeSoftWrap) {
    VisualPosition visualPosition = offsetToVisualPosition(offset, leanForward, beforeSoftWrap);
    return visualPositionToPoint2D(visualPosition);
  }

  /**
   *
   * @param visualLine
   * @return
   */
  default int visualLineToY(int visualLine) {
    return visualPositionToXY(new VisualPosition(visualLine, 0)).y;
  }

  /**
   * y 到视线
   * @param y
   * @return
   */
  default int yToVisualLine(int y) {
    return xyToVisualPosition(new Point(0, y)).line;
  }

  /**
   * 视觉线到 Y 范围
   * <br/>
   * 返回对应于给定视线的 Y 坐标范围（不包括关联的块镶嵌）。
   * <br/>
   * Returns the range of Y coordinates corresponding to the given visual line (not including associated block inlays).
   *
   * @return array of length 2, containing boundaries of the target Y range
   */
  default int @NotNull [] visualLineToYRange(int visualLine) {
    int startY = visualLineToY(visualLine);
    int startOffset = visualPositionToOffset(new VisualPosition(visualLine, 0));
    FoldRegion foldRegion = getFoldingModel().getCollapsedRegionAtOffset(startOffset);
    int endY = startY + (foldRegion instanceof CustomFoldRegion ? ((CustomFoldRegion)foldRegion).getHeightInPixels() : getLineHeight());
    return new int[] {startY, endY};
  }

  /**
   * 添加编辑器鼠标监听器
   * <br/>
   * 添加一个侦听器，用于接收有关编辑器中鼠标单击和鼠标进入退出编辑器的通知。
   * <br/>
   * Adds a listener for receiving notifications about mouse clicks in the editor and
   * the mouse entering/exiting the editor.
   *
   * @param listener the listener instance.
   */
  void addEditorMouseListener(@NotNull EditorMouseListener listener);

  /**
   * 添加编辑器鼠标监听器
   * <br/>
   * 添加一个侦听器，用于接收有关编辑器中鼠标单击和鼠标进入退出编辑器的通知。当给定的父一次性被释放时，监听器被移除。
   * <br/>
   * Adds a listener for receiving notifications about mouse clicks in the editor and
   * the mouse entering/exiting the editor.
   * The listener is removed when the given parent disposable is disposed.
   *
   * @param listener         the listener instance.
   * @param parentDisposable the parent Disposable instance.
   */
  default void addEditorMouseListener(@NotNull EditorMouseListener listener, @NotNull Disposable parentDisposable) {
    addEditorMouseListener(listener);
    Disposer.register(parentDisposable, () -> removeEditorMouseListener(listener));
  }

  /**
   * 删除编辑器鼠标监听器
   * <br/>
   * 删除一个侦听器，用于接收有关编辑器中的鼠标单击和鼠标进入退出编辑器的通知。
   * <br/>
   * Removes a listener for receiving notifications about mouse clicks in the editor and
   * the mouse entering/exiting the editor.
   *
   * @param listener the listener instance.
   */
  void removeEditorMouseListener(@NotNull EditorMouseListener listener);

  /**
   * 添加编辑器鼠标运动监听器
   *<br/>
   * 添加一个侦听器，用于在编辑器中接收有关鼠标移动的通知。
   * Adds a listener for receiving notifications about mouse movement in the editor.
   *
   * @param listener the listener instance.
   */
  void addEditorMouseMotionListener(@NotNull EditorMouseMotionListener listener);

  /**
   * 添加编辑器鼠标运动监听器
   * <br/>
   * 添加一个侦听器，用于在编辑器中接收有关鼠标移动的通知。当给定的父一次性被释放时，监听器被移除。
   * Adds a listener for receiving notifications about mouse movement in the editor.
   * The listener is removed when the given parent disposable is disposed.
   *
   * @param listener         the listener instance.
   * @param parentDisposable the parent Disposable instance.
   */
  default void addEditorMouseMotionListener(@NotNull EditorMouseMotionListener listener, @NotNull Disposable parentDisposable) {
    addEditorMouseMotionListener(listener);
    Disposer.register(parentDisposable, () -> removeEditorMouseMotionListener(listener));
  }

  /**
   * 删除一个侦听器，用于接收有关编辑器中鼠标移动的通知。
   * Removes a listener for receiving notifications about mouse movement in the editor.
   *
   * @param listener the listener instance.
   */
  void removeEditorMouseMotionListener(@NotNull EditorMouseMotionListener listener);

  /**
   * 被处置
   * <br/>
   * 检查此编辑器实例是否已被释放。
   * <br/>
   * Checks if this editor instance has been disposed.
   *
   * @return {@code true} if the editor has been disposed, {@code false} otherwise.
   */
  boolean isDisposed();

  /**
   * 获取项目
   *
   * <br/>
   * 返回编辑器相关的项目。h
   *
   * Returns the project to which the editor is related.
   *
   * @return the project instance, or {@code null} if the editor is not related to any project.
   */
  @Nullable Project getProject();

  /**
   * 是插入模式
   * <br/>
   * 返回编辑器的插入覆盖模式
   * <br/>
   * Returns the insert/overwrite mode for the editor.
   *
   * @return {@code true} if the editor is in insert mode, {@code false} otherwise.
   */
  boolean isInsertMode();

  /**
   * 是列模式
   * <br/>
   * 返回编辑器的块选择模式
   * <br/>
   * Returns the block selection mode for the editor.
   *
   * @return {@code true} if the editor uses column selection, {@code false} if it uses regular selection.
   */
  boolean isColumnMode();

  /**
   * 是单线模式
   * <br/>
   * 检查当前编辑器实例是否是单行编辑器（例如，在对话框控件中使用）。
   * <br/>
   * Checks if the current editor instance is a one-line editor (used in a dialog control, for example).
   * <br/>
   *{@code true} 如果编辑器是单行的，{@code false} 否则
   * @return {@code true} if the editor is one-line, {@code false} otherwise.
   */
  boolean isOneLineMode();

  /**
   * 得到排水沟
   * <br/>
   * 返回编辑器的装订线实例，可用于在装订线中绘制自定义文本注释。
   * Returns the gutter instance for the editor, which can be used to draw custom text annotations
   * in the gutter.
   *
   * @return the gutter instance.
   */
  @NotNull EditorGutter getGutter();

  /**
   * 获取鼠标事件区
   * <br/>
   * 返回发生指定鼠标事件的编辑器区域（文本、装订线、折叠轮廓等）。
   * Returns the editor area (text, gutter, folding outline and so on) in which the specified
   * mouse event occurred.
   *
   * @param e the mouse event for which the area is requested.  请求区域的鼠标事件。
   * @return the editor area, or {@code null} if the event occurred over an unknown area.  编辑器区域，如果事件发生在未知区域，则为 {@code null}。
   */
  @Nullable EditorMouseEventArea getMouseEventArea(@NotNull MouseEvent e);

  /**
   * 设置标题组件
   * <br/>
   * 为此文本编辑器设置标题组件。请注意，这用于文本查找功能，因此一旦用户按下 Ctrl+F，您的组件很可能会被重置。
   * Set up a header component for this text editor. Please note this is used for textual find feature so your component will most
   * probably will be reset once the user presses Ctrl+F.
   *<br/>
   * 要设置为此文本编辑器的标题的组件或 {@code null} 以删除现有的组件。
   * @param header a component to setup as header for this text editor or {@code null} to remove existing one.
   */
  void setHeaderComponent(@Nullable JComponent header);

  /**
   * 有标题组件
   * <br/>
   * 如果此编辑器具有由 setHeaderComponent(JComponent) 设置的活动标题组件，则为 true
   * @return {@code true} if this editor has active header component set up by {@link #setHeaderComponent(JComponent)}
   */
  boolean hasHeaderComponent();

  /**
   * 获取头组件
   * <br/>
   * 如果当前没有安装标头，则由 {@link setHeaderComponent(JComponent)} 或 {@code null} 设置的组件。
   * @return a component set by {@link #setHeaderComponent(JComponent)} or {@code null} if no header currently installed.
   */
  @Nullable JComponent getHeaderComponent();

  /**
   * 获取缩进模型
   * @return
   */
  @NotNull IndentsModel getIndentsModel();

  /**
   * 获取镶嵌模型
   * @return
   */
  @NotNull InlayModel getInlayModel();

  /**
   * 获取荧光笔
   * @return
   */
  @NotNull EditorKind getEditorKind();

  /**
   * 获取荧光笔
   * @return
   */
  default @NotNull EditorHighlighter getHighlighter() {
    return EditorCoreUtil.createEmptyHighlighter(getProject(), getDocument());
  }

  /**
   * 获得上升
   * <br/>
   * 视线顶部（相应坐标由 {@link visualLineToY(int)}、{@link visualPositionToXY(VisualPosition)} 等返回）和该视线中文本的基线之间的垂直距离，以像素为单位。
   * Vertical distance, in pixels, between the top of visual line (corresponding coordinate is returned by {@link #visualLineToY(int)},
   * {@link #visualPositionToXY(VisualPosition)}, etc) and baseline of text in that visual line.
   */
  default int getAscent() {
    // actual implementation in EditorImpl is a bit more complex, but this gives an idea how it's constructed
    return (int)(getContentComponent().getFontMetrics(getColorsScheme().getFont(EditorFontType.PLAIN)).getAscent() *
                 getColorsScheme().getLineSpacing());
  }
}
