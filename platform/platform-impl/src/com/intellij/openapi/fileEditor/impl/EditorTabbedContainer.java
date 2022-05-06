// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.fileEditor.impl;

import com.intellij.ide.DataManager;
import com.intellij.ide.GeneralSettings;
import com.intellij.ide.IdeEventQueue;
import com.intellij.ide.actions.CloseAction;
import com.intellij.ide.actions.MaximizeEditorInSplitAction;
import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.ui.customization.CustomActionsSchema;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.actionSystem.impl.ActionButton;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.fileEditor.ex.IdeDocumentHistory;
import com.intellij.openapi.fileEditor.impl.tabActions.CloseTab;
import com.intellij.openapi.fileEditor.impl.text.FileDropHandler;
import com.intellij.openapi.options.advanced.AdvancedSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.openapi.ui.Queryable;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.ComponentWithMnemonics;
import com.intellij.ui.ExperimentalUI;
import com.intellij.ui.InplaceButton;
import com.intellij.ui.JBColor;
import com.intellij.ui.docking.DockContainer;
import com.intellij.ui.docking.DockManager;
import com.intellij.ui.docking.DockableContent;
import com.intellij.ui.docking.DragSession;
import com.intellij.ui.docking.impl.DockManagerImpl;
import com.intellij.ui.paint.LinePainter2D;
import com.intellij.ui.tabs.*;
import com.intellij.ui.tabs.impl.*;
import com.intellij.ui.tabs.impl.tabsLayout.TabsLayoutInfo;
import com.intellij.ui.tabs.impl.tabsLayout.TabsLayoutSettingsManager;
import com.intellij.util.ObjectUtils;
import com.intellij.util.concurrency.EdtScheduledExecutorService;
import com.intellij.util.concurrency.NonUrgentExecutor;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.TimedDeadzone;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 编辑器选项卡式容器
 */
public final class EditorTabbedContainer implements CloseAction.CloseTarget {
  private final EditorWindow myWindow;
  private final Project myProject;
  private final @NotNull JBTabsEx myTabs;

  @NonNls
  public static final String HELP_ID = "ideaInterface.editor";

  private final TabInfo.DragOutDelegate myDragOutDelegate = new MyDragOutDelegate();

  EditorTabbedContainer(@NotNull EditorWindow window, @NotNull Project project, @NotNull Disposable parentDisposable) {
    myWindow = window;
    myProject = project;
    myTabs = new EditorTabs(project, parentDisposable, window);
    myTabs.getComponent().setFocusable(false);
    myTabs.getComponent().setTransferHandler(new MyTransferHandler());
    myTabs
      .setDataProvider(new MyDataProvider())
      .setPopupGroup(
        () -> (ActionGroup)CustomActionsSchema.getInstance().getCorrectedAction(IdeActions.GROUP_EDITOR_TAB_POPUP), ActionPlaces.EDITOR_TAB_POPUP, false)
      .addTabMouseListener(new TabMouseListener()).getPresentation()
      .setTabDraggingEnabled(true)
      .setTabLabelActionsMouseDeadzone(TimedDeadzone.NULL).setTabLabelActionsAutoHide(false)
      .setActiveTabFillIn(EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground()).setPaintFocus(false).getJBTabs()
      .addListener(new TabsListener() {
        @Override
        public void selectionChanged(TabInfo oldSelection, TabInfo newSelection) {
          FileEditorManager editorManager = FileEditorManager.getInstance(myProject);
          FileEditor oldEditor = oldSelection != null ? editorManager.getSelectedEditor((VirtualFile)oldSelection.getObject()) : null;
          if (oldEditor != null) {
            oldEditor.deselectNotify();
          }

          VirtualFile newFile = (VirtualFile)newSelection.getObject();
          FileEditor newEditor = editorManager.getSelectedEditor(newFile);
          if (newEditor != null) {
            newEditor.selectNotify();
          }

          if (GeneralSettings.getInstance().isSyncOnFrameActivation()) {
            VfsUtil.markDirtyAndRefresh(true, false, false, newFile);
          }
        }
      })
      .setSelectionChangeHandler((info, requestFocus, doChangeSelection) -> {
        if (myWindow.isDisposed()) return ActionCallback.DONE;
        ActionCallback result = new ActionCallback();
        CommandProcessor.getInstance().executeCommand(myProject, () -> {
          ((IdeDocumentHistoryImpl)IdeDocumentHistory.getInstance(myProject)).onSelectionChanged();
          result.notify(doChangeSelection.run());
        }, "EditorChange", null);
        return result;
      });
    myTabs.getPresentation().setRequestFocusOnLastFocusedComponent(true);
    myTabs.getComponent().addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (myTabs.findInfo(e) != null || isFloating()) return;
        if (!e.isPopupTrigger() && SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
          doProcessDoubleClick(e);
        }
      }
    });

    setTabPlacement(UISettings.getInstance().getEditorTabPlacement());

    if (JBTabsImpl.NEW_TABS) {
      TabsLayoutInfo tabsLayoutInfo = TabsLayoutSettingsManager.getInstance().getSelectedTabsLayoutInfo();
      myTabs.updateTabsLayout(tabsLayoutInfo);
    }
  }

  /**
   * 获取标签数量
   * @return
   */
  public int getTabCount() {
    return myTabs.getTabCount();
  }

  /**
   * 设置选定索引
   * @param indexToSelect
   * @return
   */
  @NotNull
  public ActionCallback setSelectedIndex(int indexToSelect) {
    return setSelectedIndex(indexToSelect, true);
  }

  /**
   * 设置选定索引
   * @param indexToSelect
   * @param focusEditor 焦点编辑器
   * @return
   */
  @NotNull
  public ActionCallback setSelectedIndex(int indexToSelect, boolean focusEditor) {
    if (indexToSelect >= myTabs.getTabCount()) return ActionCallback.REJECTED;
    return myTabs.select(myTabs.getTabAt(indexToSelect), focusEditor);
  }

  /**
   * 创建可停靠编辑器
   * @param project
   * @param image
   * @param file
   * @param presentation
   * @param window
   * @param isNorthPanelAvailable
   * @return
   */
  @NotNull
  public static DockableEditor createDockableEditor(Project project,
                                                    Image image,
                                                    VirtualFile file,
                                                    Presentation presentation,
                                                    EditorWindow window,
                                                    boolean isNorthPanelAvailable) {
    return new DockableEditor(project, image, file, presentation, window.getSize(), window.isFilePinned(file), isNorthPanelAvailable);
  }

  @NotNull
  public JComponent getComponent() {
    return myTabs.getComponent();
  }

  /**
   * 删除标签
   * @param componentIndex
   * @param indexToSelect
   * @param transferFocus
   * @return
   */
  public ActionCallback removeTabAt(int componentIndex, int indexToSelect, boolean transferFocus) {
    TabInfo toSelect = indexToSelect >= 0 && indexToSelect < myTabs.getTabCount() ? myTabs.getTabAt(indexToSelect) : null;
    TabInfo info = myTabs.getTabAt(componentIndex);
    // removing hidden tab happens on end of drag-out, we've already selected the correct tab for this case in dragOutStarted
    if (info.isHidden() || !myProject.isOpen() || myWindow.isDisposed()) {
      toSelect = null;
    }
    ActionCallback callback = myTabs.removeTab(info, toSelect, transferFocus);
    return myProject.isOpen() && !myWindow.isDisposed() ? callback : ActionCallback.DONE;
  }

  public ActionCallback removeTabAt(int componentIndex, int indexToSelect) {
    return removeTabAt(componentIndex, indexToSelect, true);
  }

  /**
   * 获取选定索引
   * @return
   */
  public int getSelectedIndex() {
    return myTabs.getIndexOf(myTabs.getSelectedInfo());
  }

  /**
   * 将前景设置为
   * @param index
   * @param color
   */
  void setForegroundAt(int index, @NotNull Color color) {
    myTabs.getTabAt(index).setDefaultForeground(color);
  }

  void setTextAttributes(int index, @Nullable TextAttributes attributes) {
    TabInfo tab = myTabs.getTabAt(index);
    tab.setDefaultAttributes(attributes);
  }

  /**
   * 设置指定标签的标签图标
   * @param index
   * @param icon
   */
  void setIconAt(int index, Icon icon) {
    myTabs.getTabAt(index).setIcon(icon);
  }

  Icon getIconAt(int index) {
    return myTabs.getTabAt(index).getIcon();
  }

  /**
   * 设置标题
   * @param index
   * @param text
   */
  void setTitleAt(int index, @NlsContexts.TabTitle @NotNull String text) {
    myTabs.getTabAt(index).setText(text);
  }

  /**
   * 将工具提示文本设置为
   * @param index
   * @param text
   */
  void setToolTipTextAt(int index, @NlsContexts.Tooltip String text) {
    myTabs.getTabAt(index).setTooltipText(text);
  }

  void setBackgroundColorAt(int index, @Nullable Color color) {
    myTabs.getTabAt(index).setTabColor(color);
  }

  /**
   * 设置选项卡布局策略
   * @param policy
   */
  void setTabLayoutPolicy(int policy) {
    switch (policy) {
      case JTabbedPane.SCROLL_TAB_LAYOUT:
        myTabs.getPresentation().setSingleRow(true);
        break;
      case JTabbedPane.WRAP_TAB_LAYOUT:
        myTabs.getPresentation().setSingleRow(false);
        break;
      default:
        throw new IllegalArgumentException("Unsupported tab layout policy: " + policy);
    }
  }

  /**
   * 设置标签放置
   * @param tabPlacement
   */
  public void setTabPlacement(int tabPlacement) {
    switch (tabPlacement) {
      case SwingConstants.TOP:
        myTabs.getPresentation().setTabsPosition(JBTabsPosition.top);
        break;
      case SwingConstants.BOTTOM:
        myTabs.getPresentation().setTabsPosition(JBTabsPosition.bottom);
        break;
      case SwingConstants.LEFT:
        myTabs.getPresentation().setTabsPosition(JBTabsPosition.left);
        break;
      case SwingConstants.RIGHT:
        myTabs.getPresentation().setTabsPosition(JBTabsPosition.right);
        break;
      case UISettings.TABS_NONE:
        myTabs.getPresentation().setHideTabs(true);
        break;
      default:
        throw new IllegalArgumentException("Unknown tab placement code=" + tabPlacement);
    }
  }

  void updateTabsLayout(@NotNull TabsLayoutInfo newTabsLayoutInfo) {
    myTabs.updateTabsLayout(newTabsLayoutInfo);
  }

  /**
   * @param ignorePopup if {@code false} and context menu is shown currently for some tab,
   *                    component for which menu is invoked will be returned
   */
  @Nullable
  public Object getSelectedComponent(boolean ignorePopup) {
    TabInfo info = ignorePopup ? myTabs.getSelectedInfo() : myTabs.getTargetInfo();
    return info != null ? info.getComponent() : null;
  }

  public void insertTab(@NotNull VirtualFile file,
                        Icon icon,
                        @NotNull JComponent component,
                        @Nullable @NlsContexts.Tooltip String tooltip,
                        int indexToInsert,
                        @NotNull Disposable parentDisposable) {
    TabInfo existing = myTabs.findInfo(file);
    if (existing != null) {
      return;
    }

    TabInfo tab = new TabInfo(component)
      .setText(file.getPresentableName())
      .setTabColor(EditorTabPresentationUtil.getEditorTabBackgroundColor(myProject, file))
      .setIcon(UISettings.getInstance().getShowFileIconInTabs() ? icon : null)
      .setTooltipText(tooltip)
      .setObject(file)
      .setDragOutDelegate(myDragOutDelegate);
    tab.setTestableUi(new MyQueryable(tab));
    ReadAction.nonBlocking(() -> EditorTabPresentationUtil.getEditorTabTitle(myProject, file))
      .expireWith(parentDisposable)
      .finishOnUiThread(ModalityState.any(), (@NlsContexts.TabTitle String title) -> tab.setText(title))
      .submit(NonUrgentExecutor.getInstance());

    CloseTab closeTab = new CloseTab(component, file, myProject, myWindow, parentDisposable);
    DataContext dataContext = DataManager.getInstance().getDataContext(component);

    DefaultActionGroup editorActionGroup = (DefaultActionGroup)ActionManager.getInstance().getAction(
      "EditorTabActionGroup");
    DefaultActionGroup group = new DefaultActionGroup();

    AnActionEvent event = AnActionEvent.createFromDataContext("EditorTabActionGroup", null, dataContext);

    for (AnAction action : editorActionGroup.getChildren(event)) {
      if(action instanceof ActionGroup) {
        group.addAll(((ActionGroup)action).getChildren(event));
      } else {
        group.addAction(action);
      }
    }
    group.addAction(closeTab, Constraints.LAST);

    tab.setTabLabelActions(group, ActionPlaces.EDITOR_TAB);

    myTabs.addTabSilently(tab, indexToInsert);
  }

  boolean isEmptyVisible() {
    return myTabs.isEmptyVisible();
  }

  public JBTabs getTabs() {
    return myTabs;
  }

  public void requestFocus(boolean forced) {
    IdeFocusManager.getInstance(myProject).requestFocus(myTabs.getComponent(), forced);
  }

  /**
   * 我的可查询
   */
  private static class MyQueryable implements Queryable {
    private final TabInfo myTab;

    MyQueryable(TabInfo tab) {
      myTab = tab;
    }

    /**
     * 存放信息
     * @param info
     */
    @Override
    public void putInfo(@NotNull Map<? super String, ? super String> info) {
      info.put("editorTab", myTab.getText());
    }
  }

  /**
   * 获取组件
   * @param i
   * @return
   */
  public Component getComponentAt(int i) {
    TabInfo tab = myTabs.getTabAt(i);
    return tab.getComponent();
  }

  /**
   * 我的数据提供者
   */
  private final class MyDataProvider implements DataProvider {
    @Override
    public Object getData(@NotNull @NonNls String dataId) {
      if (CommonDataKeys.PROJECT.is(dataId)) {
        return myProject;
      }
      if (CommonDataKeys.VIRTUAL_FILE.is(dataId)) {
        VirtualFile selectedFile = myWindow.getSelectedFile();
        return selectedFile != null && selectedFile.isValid() ? selectedFile : null;
      }
      if (EditorWindow.DATA_KEY.is(dataId)) {
        return myWindow;
      }
      if (PlatformCoreDataKeys.HELP_ID.is(dataId)) {
        return HELP_ID;
      }

      if (CloseAction.CloseTarget.KEY.is(dataId)) {
        TabInfo selected = myTabs.getSelectedInfo();
        if (selected != null) {
          return EditorTabbedContainer.this;
        }
      }

      if (EditorWindow.DATA_KEY.is(dataId)) {
        return myWindow;
      }

      return null;
    }
  }

  @Override
  public void close() {
    TabInfo selected = myTabs.getTargetInfo();
    if (selected == null) return;
    FileEditorManagerEx.getInstanceEx(myProject).closeFile((VirtualFile)selected.getObject(), myWindow);
  }

  /**
   * 是浮动的
   * @return
   */
  private boolean isFloating() {
    return myWindow.getOwner().isFloating();
  }

  /**
   * 标签鼠标监听器
   */
  private class TabMouseListener extends MouseAdapter {
    /**
     * 我的操作点击次数
     */
    private int myActionClickCount;

    /**
     * 鼠标释放
     * @param e
     */
    @Override
    public void mouseReleased(MouseEvent e) {
      if (UIUtil.isCloseClick(e, MouseEvent.MOUSE_RELEASED)) {
        TabInfo info = myTabs.findInfo(e);
        if (info != null) {
          IdeEventQueue.getInstance().blockNextEvents(e);
          if (e.isAltDown() && e.getButton() == MouseEvent.BUTTON1) {//close others
            List<TabInfo> allTabInfos = myTabs.getTabs();
            for (TabInfo tabInfo : allTabInfos) {
              if (tabInfo == info) continue;
              FileEditorManagerEx.getInstanceEx(myProject).closeFile((VirtualFile)tabInfo.getObject(), myWindow);
            }
          } else {
            FileEditorManagerEx.getInstanceEx(myProject).closeFile((VirtualFile)info.getObject(), myWindow);
          }
        }
      }
    }

    /**
     * 鼠标按下
     * @param e
     */
    @Override
    public void mousePressed(MouseEvent e) {
      if (UIUtil.isActionClick(e)) {
        if (e.getClickCount() == 1) {
          myActionClickCount = 0;
        }
        // clicks on the close window button don't count in determining whether we have a double-click on tab (IDEA-70403)
        Component deepestComponent = SwingUtilities.getDeepestComponentAt(e.getComponent(), e.getX(), e.getY());
        if (!(deepestComponent instanceof InplaceButton)) {
          myActionClickCount++;
        }
        if (myActionClickCount > 1 && myActionClickCount % 2 == 0) {
          doProcessDoubleClick(e);
        }
      }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
      if (UIUtil.isActionClick(e, MouseEvent.MOUSE_CLICKED) && (e.isMetaDown() || !SystemInfo.isMac && e.isControlDown())) {
        TabInfo info = myTabs.findInfo(e);
        Object o = info == null ? null : info.getObject();
        if (o instanceof VirtualFile) {
          ShowFilePathAction.show((VirtualFile)o, e);
        }
      }
    }
  }

  /**
   * 双击执行的操作
   * @param e
   */
  private static void doProcessDoubleClick(@NotNull MouseEvent e) {
    if (!AdvancedSettings.getBoolean("editor.maximize.on.double.click") && !AdvancedSettings.getBoolean("editor.maximize.in.splits.on.double.click")) return;
    ActionManager actionManager = ActionManager.getInstance();
    DataContext context = DataManager.getInstance().getDataContext();
    Boolean isEditorMaximized = null;
    Boolean areAllToolWindowsHidden = null;
    if (AdvancedSettings.getBoolean("editor.maximize.in.splits.on.double.click")) {
      AnAction maximizeEditorInSplit = actionManager.getAction("MaximizeEditorInSplit");
      if (maximizeEditorInSplit != null) {
        AnActionEvent event = new AnActionEvent(e, context, ActionPlaces.EDITOR_TAB, new Presentation(), actionManager, e.getModifiersEx());
        maximizeEditorInSplit.update(event);
        isEditorMaximized = event.getPresentation().getClientProperty(MaximizeEditorInSplitAction.Companion.getCURRENT_STATE_IS_MAXIMIZED_KEY());
      }
    }
    if (AdvancedSettings.getBoolean("editor.maximize.on.double.click")) {
      AnAction hideAllToolWindows = actionManager.getAction("HideAllWindows");
      if (hideAllToolWindows != null) {
        AnActionEvent event = new AnActionEvent(e, context, ActionPlaces.EDITOR_TAB, new Presentation(), actionManager, e.getModifiersEx());
        hideAllToolWindows.update(event);
        areAllToolWindowsHidden = event.getPresentation().getClientProperty(MaximizeEditorInSplitAction.Companion.getCURRENT_STATE_IS_MAXIMIZED_KEY());
      }
    }
    Runnable runnable = Registry.is("editor.position.mouse.cursor.on.doubleclicked.tab")
                        ? createKeepMousePositionRunnable(e)
                        : null;
    if (areAllToolWindowsHidden != null && (isEditorMaximized == null || isEditorMaximized == areAllToolWindowsHidden)) {
      actionManager.tryToExecute(actionManager.getAction("HideAllWindows"), e, null, ActionPlaces.EDITOR_TAB, true);
    }
    if (isEditorMaximized != null) {
      actionManager.tryToExecute(actionManager.getAction("MaximizeEditorInSplit"), e, null, ActionPlaces.EDITOR_TAB, true);
    }
    ObjectUtils.consumeIfNotNull(runnable, Runnable::run);
  }

  /**
   * 创建保持鼠标位置可运行
   * @param event
   * @return
   */
  @NotNull
  private static Runnable createKeepMousePositionRunnable(@NotNull MouseEvent event) {
    return () -> EdtScheduledExecutorService.getInstance().schedule(() -> {
      Component component = event.getComponent();
      if (component != null && component.isShowing()) {
        Point p = component.getLocationOnScreen();
        p.translate(event.getX(), event.getY());
        try {
          new Robot().mouseMove(p.x, p.y);
        }
        catch (AWTException ignored) {
        }
      }
    }, 50, TimeUnit.MILLISECONDS);
  }

  /**
   * 进程拆分
   */
  public void processSplit() {
    final TabInfo tabInfo = this.myTabs.getSelectedInfo();
    if (tabInfo == null) {
      return;
    }

    Image img = JBTabsImpl.getComponentImage(tabInfo);
    VirtualFile file = (VirtualFile)tabInfo.getObject();
    Presentation presentation = new Presentation(tabInfo.getText());
    presentation.setIcon(tabInfo.getIcon());
    EditorComposite composite = myWindow.getComposite(file);
    FileEditor[] editors = composite != null ? composite.getAllEditors().toArray(FileEditor.EMPTY_ARRAY) : FileEditor.EMPTY_ARRAY;
    final DockableEditor dockableEditor = createDockableEditor(myProject, img, file, presentation, myWindow, DockManagerImpl.isNorthPanelAvailable(editors));
  }

  /**
   * 我的拖出代表
   */
  class MyDragOutDelegate implements TabInfo.DragOutDelegate {

    /**
     * 虚拟文件
     */
    private VirtualFile myFile;
    /**
     * 拖动会话
     */
    private DragSession mySession;

    /**
     * 拖出开始
     * @param mouseEvent
     * @param info
     */
    @Override
    public void dragOutStarted(@NotNull MouseEvent mouseEvent, @NotNull TabInfo info) {
      TabInfo previousSelection = info.getPreviousSelection();
      Image img = JBTabsImpl.getComponentImage(info);
      if (previousSelection == null) {
        previousSelection = myTabs.getToSelectOnRemoveOf(info);
      }
      int dragStartIndex = myTabs.getIndexOf(info);
      boolean isPinnedAtStart = info.isPinned();
      info.setHidden(true);
      if (previousSelection != null) {
        myTabs.select(previousSelection, true);
      }

      myFile = (VirtualFile)info.getObject();
      myFile.putUserData(EditorWindow.DRAG_START_INDEX_KEY, dragStartIndex);
      myFile.putUserData(EditorWindow.DRAG_START_LOCATION_HASH_KEY, System.identityHashCode(myTabs));
      myFile.putUserData(EditorWindow.DRAG_START_PINNED_KEY, isPinnedAtStart);
      Presentation presentation = new Presentation(info.getText());
      if (DockManagerImpl.REOPEN_WINDOW.isIn(myFile)) {
        presentation.putClientProperty(DockManagerImpl.REOPEN_WINDOW, DockManagerImpl.REOPEN_WINDOW.get(myFile, true));
      }
      presentation.setIcon(info.getIcon());
      EditorComposite composite = myWindow.getComposite(myFile);
      FileEditor[] editors = composite != null ? composite.getAllEditors().toArray(FileEditor.EMPTY_ARRAY) : FileEditor.EMPTY_ARRAY;
      boolean isNorthPanelAvailable = DockManagerImpl.isNorthPanelAvailable(editors);
      mySession = getDockManager()
        .createDragSession(mouseEvent, createDockableEditor(myProject, img, myFile, presentation, myWindow, isNorthPanelAvailable));
    }

    /**
     * 获取容器管理器
     * @return
     */
    private DockManager getDockManager() {
      return DockManager.getInstance(myProject);
    }

    /**
     * 进程拖出
     * @param event
     * @param source
     */
    @Override
    public void processDragOut(@NotNull MouseEvent event, @NotNull TabInfo source) {
      mySession.process(event);
    }

    /**
     * 拖出完成
     * @param event
     * @param source
     */
    @Override
    public void dragOutFinished(@NotNull MouseEvent event, TabInfo source) {
      boolean copy = UIUtil.isControlKeyDown(event) || mySession.getResponse(event) == DockContainer.ContentResponse.ACCEPT_COPY;
      if (!copy) {
        myFile.putUserData(FileEditorManagerImpl.CLOSING_TO_REOPEN, Boolean.TRUE);
        FileEditorManagerEx.getInstanceEx(myProject).closeFile(myFile, myWindow);
      }
      else {
        source.setHidden(false);
      }

      mySession.process(event);
      if (!copy) {
        myFile.putUserData(FileEditorManagerImpl.CLOSING_TO_REOPEN, null);
      }

      myFile = null;
      mySession = null;
    }

    /**
     * 拖出取消
     * @param source
     */
    @Override
    public void dragOutCancelled(TabInfo source) {
      source.setHidden(false);
      if (mySession != null) {
        mySession.cancel();
      }

      myFile = null;
      mySession = null;
    }

  }

  /**
   * 可停靠编辑器
   *
   * 可停靠内容<虚拟问件>
   */
  public static class DockableEditor implements DockableContent<VirtualFile> {
    final Image myImg;
    /**
     * 介绍
     */
    private final Presentation myPresentation;
    /**
     * 首选尺寸
     */
    private final Dimension myPreferredSize;
    /**
     * 固定
     */
    private final boolean myPinned;
    /**
     * 北面板可用
     */
    private final boolean myNorthPanelAvailable;
    /**
     * 虚拟文件
     */
    private final VirtualFile myFile;

    public DockableEditor(Project project,
                          Image img,
                          VirtualFile file,
                          Presentation presentation,
                          Dimension preferredSize,
                          boolean isFilePinned) {
      this(project, img, file, presentation, preferredSize, isFilePinned, DockManagerImpl.isNorthPanelVisible(UISettings.getInstance()));
    }

    public DockableEditor(Project project,
                          Image img,
                          VirtualFile file,
                          Presentation presentation,
                          Dimension preferredSize,
                          boolean isFilePinned,
                          boolean isNorthPanelAvailable) {
      myImg = img;
      myFile = file;
      myPresentation = presentation;
      myPreferredSize = preferredSize;
      myPinned = isFilePinned;
      myNorthPanelAvailable = isNorthPanelAvailable;
    }

    @NotNull
    @Override
    public VirtualFile getKey() {
      return myFile;
    }

    @Override
    public Image getPreviewImage() {
      return myImg;
    }

    @Override
    public Dimension getPreferredSize() {
      return myPreferredSize;
    }

    @Override
    public String getDockContainerType() {
      return DockableEditorContainerFactory.TYPE;
    }

    @Override
    public Presentation getPresentation() {
      return myPresentation;
    }

    @Override
    public void close() {
    }

    public VirtualFile getFile() {
      return myFile;
    }

    public boolean isPinned() {
      return myPinned;
    }

    public boolean isNorthPanelAvailable() {
      return myNorthPanelAvailable;
    }
  }

  /**
   * 我的传输处理程序
   */
  private final class MyTransferHandler extends TransferHandler {
    private final FileDropHandler myFileDropHandler = new FileDropHandler(null);

    /**
     * 导入数据
     * @param comp
     * @param t
     * @return
     */
    @Override
    public boolean importData(JComponent comp, Transferable t) {
      if (myFileDropHandler.canHandleDrop(t.getTransferDataFlavors())) {
        myFileDropHandler.handleDrop(t, myProject, myWindow);
        return true;
      }
      return false;
    }

    /**
     * 可以导入
     * @param comp
     * @param transferFlavors
     * @return
     */
    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
      return myFileDropHandler.canHandleDrop(transferFlavors);
    }
  }

  /**
   * 编辑器选项卡
   */
  private static final class EditorTabs extends SingleHeightTabs implements ComponentWithMnemonics {
    @NotNull
    private final EditorWindow myWindow;

    private EditorTabs(Project project, @NotNull Disposable parentDisposable, @NotNull EditorWindow window) {
      super(project, parentDisposable);

      myWindow = window;
      UIUtil.addAwtListener(e -> updateActive(), AWTEvent.FOCUS_EVENT_MASK, parentDisposable);
      setUiDecorator(() -> new UiDecorator.UiDecoration(null, JBUI.CurrentTheme.EditorTabs.tabInsets()));

      project.getMessageBus().connect(parentDisposable).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
        @Override
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          updateActive();
        }

        @Override
        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
          updateActive();
        }

        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
          updateActive();
        }
      });
    }

    /**
     * 支持表格布局为单行
     * @return
     */
    @Override
    protected boolean supportsTableLayoutAsSingleRow() {
      return true;
    }

    /**
     * 绘制 子节点
     * @param g
     */
    @Override
    protected void paintChildren(Graphics g) {
      if (!isHideTabs() && ExperimentalUI.isNewEditorTabs()) {
        TabLabel label = getSelectedLabel();
        if (label != null) {
          int h = label.getHeight();
          Color color = JBColor.namedColor("EditorTabs.underTabsBorderColor", myTabPainter.getTabTheme().getBorderColor());
          g.setColor(color);
          LinePainter2D.paint(((Graphics2D)g), 0, h, getWidth(), h);
        }
      }
      super.paintChildren(g);
      drawBorder(g);
    }

    /**
     * 获取入口点操作组
     * @return
     */
    @Override
    protected DefaultActionGroup getEntryPointActionGroup() {
      AnAction source = ActionManager.getInstance().getAction("EditorTabsEntryPoint");
      source.getTemplatePresentation().putClientProperty(ActionButton.HIDE_DROPDOWN_ICON, Boolean.TRUE);
      return new DefaultActionGroup(source);
    }

    /**
     * 创建新的标签页
     * @param info
     * @return
     */
    @NotNull
    @Override
    protected TabLabel createTabLabel(@NotNull TabInfo info) {
      /**
       * 单一高度标签
       */
      return new SingleHeightLabel(this, info) {
        /**
         * 获取首选高度
         * @return
         */
        @Override
        protected int getPreferredHeight() {
          Insets insets = getInsets();
          Insets layoutInsets = getLayoutInsets();

          insets.top += layoutInsets.top;
          insets.bottom += layoutInsets.bottom;

          if (ExperimentalUI.isNewEditorTabs()) {
            insets.top -= 7;
          }
          return super.getPreferredHeight() - insets.top - insets.bottom;
        }

        /**
         * 绘制
         * @param g
         */
        @Override
        public void paint(Graphics g) {
          if (ExperimentalUI.isNewEditorTabs() && getSelectedInfo() != info && !isHoveredTab(this)) {
            GraphicsConfig config = GraphicsUtil.paintWithAlpha(g, JBUI.getFloat("EditorTabs.hoverAlpha", 0.75f));
            super.paint(g);
            config.restore();
          } else {
            super.paint(g);
          }
        }
      };
    }

    /**
     * 创建 Tab Painter 适配器
     * @return
     */
    @Override
    protected TabPainterAdapter createTabPainterAdapter() {
      return new EditorTabPainterAdapter();
    }

    /**
     * 创建标签边框
     * @return
     */
    @Override
    protected JBTabsBorder createTabBorder() {
      return new JBEditorTabsBorder(this);
    }

    /**
     * 活动
     */
    private boolean active;

    @NotNull
    @Override
    public ActionCallback select(@NotNull TabInfo info, boolean requestFocus) {
      active = true;
      return super.select(info, requestFocus);
    }

    private void updateActive() {
      checkActive();
      SwingUtilities.invokeLater(() -> {
        checkActive();
      });
    }

    private void checkActive() {
      boolean newActive = UIUtil.isFocusAncestor(this);

      if(newActive != active) {
        active = newActive;
        revalidateAndRepaint();
      }
    }

    /**
     * 是活动标签
     * @param info
     * @return
     */
    @Override
    protected boolean isActiveTabs(TabInfo info) {
      return active;
    }

    /**
     * 获取选择删除
     * @param info
     * @return
     */
    @Nullable
    @Override
    public TabInfo getToSelectOnRemoveOf(TabInfo info) {
      if (myWindow.isDisposed()) return null;
      int index = getIndexOf(info);
      if (index != -1) {
        VirtualFile file = myWindow.getFileAt(index);
        int indexToSelect = myWindow.calcIndexToSelect(file, index);
        if (indexToSelect >= 0 && indexToSelect < getTabs().size()) {
          return getTabAt(indexToSelect);
        }
      }
      return super.getToSelectOnRemoveOf(info);
    }

    /**
     * 重新验证并重绘
     * @param layoutNow
     */
    @Override
    public void revalidateAndRepaint(boolean layoutNow) {
      //noinspection ConstantConditions - called from super constructor
      if (myWindow != null && myWindow.getOwner().isInsideChange()) return;
      super.revalidateAndRepaint(layoutNow);
    }
  }
}
