// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.editor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.event.BulkAwareDocumentListener;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.util.text.CharArrayUtil;
import org.jetbrains.annotations.*;

import java.beans.PropertyChangeListener;

/**
 * 表示加载到内存中并可能在 IDE 文本编辑器中打开的文本文档的内容。文档文本中的换行符始终规范化为单个 {@code\n} 字符，并在保存文档时转换为正确的格式。
 * <br/>
 * Represents the contents of a text file loaded into memory, and possibly opened in an IDE
 * text editor. Line breaks in the document text are always normalized as single {@code \n} characters,
 * and are converted to proper format when the document is saved.
 * <p/>
 * Please see <a href="https://plugins.jetbrains.com/docs/intellij/documents.html">IntelliJ Platform Docs</a>.
 * for high-level overview.
 *
 * @see Editor#getDocument()
 * @see com.intellij.psi.PsiDocumentManager
 * @see com.intellij.openapi.fileEditor.FileDocumentManager
 * @see EditorFactory#createDocument(CharSequence)
 */
public interface Document extends UserDataHolder {
  /**
   * 空文档
   */
  Document[] EMPTY_ARRAY = new Document[0];
  /**
   * 属性可写
   */
  @NonNls
  String PROP_WRITABLE = "writable";

  /**
   * 获取文本内容的副本
   * <br/>
   * 处于性能原因
   * Retrieves a copy of the document content. For obvious performance reasons use
   * {@link #getCharsSequence()} whenever it's possible.
   *
   * @return document content.
   */
  @NotNull
  @Contract(pure = true)
  default @NlsSafe String getText() {
    return getImmutableCharSequence().toString();
  }

  /**
   * 获取范围内的文本内容
   * @param range
   * @return
   */
  @NotNull
  @Contract(pure = true)
  default @NlsSafe String getText(@NotNull TextRange range) {
    return range.substring(getText());
  }

  /**
   * 如果不需要创建内容的副本，请使用此方法而不是 getText（）。
   * 只要通过删除/替换/插入字符串方法调用修改文档，则由返回的 CharSequence 表示的内容可能会更改。
   * 有必要获取Application.runWriteAction（）来修改文档的内容，这样就不会出现线程问题。
   * <br/>
   *
   * Use this method instead of {@link #getText()} if you do not need to create a copy of the content.
   * Content represented by returned CharSequence is subject to change whenever document is modified via delete/replace/insertString method
   * calls. It is necessary to obtain Application.runWriteAction() to modify content of the document though so threading issues won't
   * arise.
   *
   * @return inplace document content.
   * @see #getTextLength()
   */
  @Contract(pure = true)
  @NotNull
  default @NlsSafe CharSequence getCharsSequence() {
    return getImmutableCharSequence();
  }

  /**
   * 返回 表示保证不可变的文档内容的字符串行。不需要读取或写入操作。
   * @return a char sequence representing document content that's guaranteed to be immutable. No read- or write-action is necessary.
   * @see com.intellij.util.text.ImmutableCharSequence
   */
  @NotNull
  @Contract(pure = true)
  @NlsSafe CharSequence getImmutableCharSequence();

  /**
   * 被弃用了。
   *
   * @deprecated Use {@link #getCharsSequence()} or {@link #getText()} instead.
   */
  @Deprecated
  @ApiStatus.ScheduledForRemoval
  default char @NotNull [] getChars() {
    return CharArrayUtil.fromSequence(getImmutableCharSequence());
  }

  /**
   * 返回文档文本的长度。
   * <br/>
   * Returns the length of the document text.
   *
   * @return the length of the document text.
   * @see #getCharsSequence()
   */
  @Contract(pure = true)
  default int getTextLength() {
    return getImmutableCharSequence().length();
  }

  /**
   * 返回文档中的行数。
   * <br/>
   * Returns the number of lines in the document.
   *
   * @return the number of lines in the document.
   */
  @Contract(pure = true)
  int getLineCount();

  /**
   * 返回与文档中指定的偏移量对应的行号（从 0 开始）。
   * <br/>
   * Returns the line number (0-based) corresponding to the specified offset in the document.
   *
   * @param offset the offset to get the line number for (must be in the range from 0 (inclusive)
   *               to {@link #getTextLength()} (inclusive)).
   * @return the line number corresponding to the offset.
   */
  @Contract(pure = true)
  int getLineNumber(int offset);

  /**
   * 返回指定行的起始偏移量
   * <br/>
   * 返回具有指定编号的行的起始偏移量。
   * <br/>
   *
   * Returns the start offset for the line with the specified number.
   *
   * @param line the line number (from 0 to getLineCount()-1)
   * @return the start offset for the line.
   */
  @Contract(pure = true)
  int getLineStartOffset(int line);

  /**
   * 返回指定行的结束偏移量
   * Returns the end offset for the line with the specified number.
   *
   * @param line the line number (from 0 to getLineCount()-1)
   * @return the end offset for the line.
   */
  @Contract(pure = true)
  int getLineEndOffset(int line);

  /**
   * 自文档保存以来，具有给定索引的行是否已被修改
   * @return whether the line with the given index has been modified since the document has been saved
   */
  default boolean isLineModified(int line) {
    return false;
  }

  /**
   * 在文档中的指定偏移处插入指定的文本。插入文本中的换行符必须规范化为 \n。
   * <br/>
   * Inserts the specified text at the specified offset in the document. Line breaks in
   * the inserted text must be normalized as \n.
   *
   * @param offset the offset to insert the text at.
   * @param s      the text to insert.
   * @throws ReadOnlyModificationException         if the document is read-only.
   * @throws ReadOnlyFragmentModificationException if the fragment to be modified is covered by a guarded block.
   */
  void insertString(int offset, @NonNls @NotNull CharSequence s);

  /**
   * 在文档中删除特定范围的文本
   * <br/>
   * Deletes the specified range of text from the document.
   *
   * @param startOffset the start offset of the range to delete.
   * @param endOffset   the end offset of the range to delete.
   * @throws ReadOnlyModificationException         if the document is read-only.
   * @throws ReadOnlyFragmentModificationException if the fragment to be modified is covered by a guarded block.
   */
  void deleteString(int startOffset, int endOffset);

  /**
   * 在文档中 替换指定范围内的文本为特定的字符串
   * <br/>
   * Replaces the specified range of text in the document with the specified string.
   * 在文本中，换行符被规范化成 \n
   * <br/>
   * Line breaks in the text to replace with must be normalized as \n.
   *
   * @param startOffset the start offset of the range to replace.
   * @param endOffset   the end offset of the range to replace.
   * @param s           the text to replace with.
   * @throws ReadOnlyModificationException         if the document is read-only.
   * @throws ReadOnlyFragmentModificationException if the fragment to be modified is covered by a guarded block.
   */
  void replaceString(int startOffset, int endOffset, @NlsSafe @NotNull CharSequence s);

  /**
   * 检查文档是否可写
   * <br/>
   * Checks if the document text is read-only.
   *
   * @return {@code true} if the document text is writable, {@code false} if it is read-only.
   * @see #fireReadOnlyModificationAttempt()
   */
  @Contract(pure = true)
  boolean isWritable();

  /**
   * 获取修改标记值。修改戳记是通过对文档内容的任何修改而更改的值。请注意，它与文档修改时间无关。
   * <br/>
   * Gets the modification stamp value. Modification stamp is a value changed by any modification
   * of the content of the file. Note that it is not related to the file modification time.
   *
   * @return the modification stamp value.
   * @see com.intellij.psi.PsiFile#getModificationStamp()
   * @see com.intellij.openapi.vfs.VirtualFile#getModificationStamp()
   */
  @Contract(pure = true)
  long getModificationStamp();

  /**
   * 触发用户希望从文档中删除只读状态的通知（可以通过从版本控制系统中签出文档或清除文档上的只读属性来删除只读状态）。
   * <br/>
   * Fires a notification that the user would like to remove the read-only state
   * from the document (the read-only state can be removed by checking the file out
   * from the version control system, or by clearing the read-only attribute on the file).
   */
  default void fireReadOnlyModificationAttempt() {
  }

  /**
   * Adds a listener for receiving notifications about changes in the document content.
   *
   * @param listener the listener instance.
   */
  default void addDocumentListener(@NotNull DocumentListener listener) {
  }

  default void addDocumentListener(@NotNull DocumentListener listener, @NotNull Disposable parentDisposable) {
  }

  /**
   * Removes a listener for receiving notifications about changes in the document content, previously added via {@link #addDocumentListener(DocumentListener)}.
   * Don't call this method for listeners added via {@link #addDocumentListener(DocumentListener, Disposable)}, as that might cause memory leaks.
   *
   * @param listener the listener instance.
   */
  default void removeDocumentListener(@NotNull DocumentListener listener) {
  }

  /**
   * 创建一个范围标记，该标记指向文档中指定的文本范围，并在文档文本更改时自动调整。标记因对文档文本的外部更改（例如，从磁盘重新加载文档）而失效。
   * <br/>
   * Creates a range marker which points to the specified range of text in the document and
   * is automatically adjusted when the document text is changed. The marker is invalidated
   * by external changes to the document text (for example, reloading the file from disk).
   *
   * @param startOffset the start offset for the range of text covered by the marker.
   * @param endOffset   the end offset for the range of text covered by the marker.
   * @return the marker instance.
   */
  @NotNull
  default RangeMarker createRangeMarker(int startOffset, int endOffset) {
    return createRangeMarker(startOffset, endOffset, false);
  }

  /**
   * Creates a range marker which points to the specified range of text in the document and
   * is automatically adjusted when the document text is changed. The marker is optionally
   * invalidated by external changes to the document text (for example, reloading the file from disk).
   *
   * @param startOffset             the start offset for the range of text covered by the marker.
   * @param endOffset               the end offset for the range of text covered by the marker.
   * @param surviveOnExternalChange if true, the marker is not invalidated by external changes. <br/>如果为 true，则标记不会因外部更改而失效
   * @return the marker instance.
   */
  @NotNull
  RangeMarker createRangeMarker(int startOffset, int endOffset, boolean surviveOnExternalChange);

  /**
   * Adds a listener for receiving notifications about changes in the properties of the document
   * (for example, its read-only state).
   *
   * @param listener the listener instance.
   */
  default void addPropertyChangeListener(@NotNull PropertyChangeListener listener) {
  }

  /**
   * Removes a listener for receiving notifications about changes in the properties of the document
   * (for example, its read-only state).
   *
   * @param listener the listener instance.
   */
  default void removePropertyChangeListener(@NotNull PropertyChangeListener listener) {
  }

  /**
   * 将文档标记为只读或读/写。此方法仅修改存储在文档实例中的标志 - 不执行签出或文档更改。
   * <br/>
   * Marks the document as read-only or read/write. This method only modifies the flag stored
   * in the document instance - no checkouts or file changes are performed.
   *
   * @param isReadOnly the new value of the read-only flag.
   * @see #isWritable()
   * @see #fireReadOnlyModificationAttempt()
   */
  default void setReadOnly(boolean isReadOnly) {
  }

  /**
   * 创建保护块
   * <br/>
   *将文档中的文本范围标记为只读（尝试修改区域中的文本会导致引发 ReadOnlyFragmentModificationException）。
   * <br/>
   * Marks a range of text in the document as read-only (attempts to modify text in the
   * range cause {@link ReadOnlyFragmentModificationException} to be thrown).
   *
   * @param startOffset the start offset of the text range to mark as read-only.
   * @param endOffset   the end offset of the text range to mark as read-only.
   * @return the marker instance.
   * @see #removeGuardedBlock(RangeMarker)
   * @see #startGuardedBlockChecking()
   * @see com.intellij.openapi.editor.actionSystem.EditorActionManager#setReadonlyFragmentModificationHandler(com.intellij.openapi.editor.actionSystem.ReadonlyFragmentModificationHandler)
   */
  @NotNull
  RangeMarker createGuardedBlock(int startOffset, int endOffset);

  /**
   * Removes a marker marking a range of text in the document as read-only.
   *
   * @param block the marker to remove.
   * @see #createGuardedBlock(int, int)
   */
  default void removeGuardedBlock(@NotNull RangeMarker block) {
  }

  /**
   * 返回复盖文档中指定偏移量的只读标记。
   * <br/>
   * Returns the read-only marker covering the specified offset in the document.
   *
   * @param offset the offset for which the marker is requested.
   * @return the marker instance, or {@code null} if the specified offset is not covered by a read-only marker.
   */
  @Nullable
  default RangeMarker getOffsetGuard(int offset) {
    return getRangeGuard(offset, offset);
  }

  /**
   * 从指定范围获取只读标记
   * Returns the read-only marker covering the specified range in the document.
   *
   * @param start the start offset of the range for which the marker is requested.
   * @param end   the end offset of the range for which the marker is requested.
   * @return the marker instance, or {@code null} if the specified range is not covered by a read-only marker.
   */
  @Nullable
  default RangeMarker getRangeGuard(int start, int end) {
    return null;
  }

  /**
   * 允许在修改文档时检查只读标记。默认情况下，检查处于禁用状态。
   * <br/>
   * Enables checking for read-only markers when the document is modified. Checking is disabled by default.
   *
   * @see #createGuardedBlock(int, int)
   * @see #stopGuardedBlockChecking()
   */
  default void startGuardedBlockChecking() {
  }

  /**
   * Disables checking for read-only markers when the document is modified. Checking is disabled by default.
   *
   * @see #createGuardedBlock(int, int)
   * @see #startGuardedBlockChecking()
   */
  default void stopGuardedBlockChecking() {
  }

  /**
   * 设置用于文档的循环缓冲区的最大大小。如果文档使用循环缓冲区，则添加到文档末尾的文本超过最大大小会导致文本从文档开头删除。
   * <br/>
   * Sets the maximum size of the cyclic buffer used for the document. If the document uses
   * a cyclic buffer, text added to the end of the document exceeding the maximum size causes
   * text to be removed from the beginning of the document.
   *
   * @param bufferSize the cyclic buffer size, or 0 if the document should not use a cyclic buffer.
   */
  default void setCyclicBufferSize(int bufferSize) {
  }

  void setText(@NotNull final CharSequence text);

  @NotNull
  default RangeMarker createRangeMarker(@NotNull TextRange textRange) {
    return createRangeMarker(textRange.getStartOffset(), textRange.getEndOffset());
  }

  @Contract(pure = true)
  default int getLineSeparatorLength(int line) {
    return 0;
  }

  /**
   * @see #setInBulkUpdate(boolean)
   */
  default boolean isInBulkUpdate() {
    return false;
  }

  /**
   * Enters or exits 'bulk' mode for processing of document changes. Bulk mode should be used when a large number of document changes
   * are applied in batch (without user interaction for each change), to improve performance. E.g. this mode is sometimes used by the
   * platform code during code formatting. In this mode some activities that usually happen on each document change will be muted, with
   * reconciliation happening on bulk mode exit.
   * <p>
   * As the reconciliation after exiting bulk mode implies some additional overhead, bulk mode shouldn't be used if the number of document
   * changes to be performed is relatively small. The number of changes which justifies switching to bulk mode is usually determined
   * empirically, but typically it's around hundred(s) of changes.
   * <p>
   * In bulk mode editor(s) associated with the document will stop updating internal caches on each document change. As a result, certain
   * operations with editor can return invalid results or lead to exception, if they are preformed in bulk mode. They include: querying
   * or updating folding or soft wrap data, editor position recalculation functions (offset to logical position, logical to visual position,
   * etc.), querying or updating caret position or selection state.
   * <p>
   * Bulk mode shouldn't span more than one thread or EDT event. Typically, it should be turned on/off in a try/finally statement.
   *
   * @see com.intellij.util.DocumentUtil#executeInBulk(Document, boolean, Runnable)
   * @see BulkAwareDocumentListener
   * @deprecated use {@link com.intellij.util.DocumentUtil#executeInBulk(Document, boolean, Runnable)} instead
   */
  @Deprecated
  default void setInBulkUpdate(boolean value) {}
}
