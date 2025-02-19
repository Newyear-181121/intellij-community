package com.intellij.smartUpdate

import com.intellij.ide.RecentProjectsManagerBase
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.jetbrains.annotations.Nls
import org.jetbrains.ide.ToolboxSettingsActionRegistry

const val IDE_UPDATE = "ide.update"
const val IDE_RESTART = "ide.restart"

class IdeUpdateStep: SmartUpdateStep {
  override val id = IDE_UPDATE

  override fun performUpdateStep(project: Project, e: AnActionEvent?, onSuccess: () -> Unit) {
    val updateAction = getUpdateAction()
    if (updateAction != null && e != null) {
      updateAction.actionPerformed(e)
      project.service<SmartUpdate>().restartRequested = true
    }
    else onSuccess()
  }

  override fun isAvailable(): Boolean {
    return getUpdateAction() != null
  }

  @Nls
  fun getDescription(): String {
    val updateAction = getUpdateAction()
    if (updateAction == null) return SmartUpdateBundle.message("no.updates.available")
    return updateAction.templatePresentation.text
  }
}

private fun getUpdateAction() = service<ToolboxSettingsActionRegistry>().getActions().find { it.isIdeUpdate }

class IdeRestartStep: SmartUpdateStep {
  override val id = IDE_RESTART

  override fun performUpdateStep(project: Project, e: AnActionEvent?, onSuccess: () -> Unit) {
    val updateAction = getUpdateAction()
    if (updateAction != null && e != null && updateAction.isRestartRequired) {
      RecentProjectsManagerBase.getInstanceEx().forceReopenProjects()
      PropertiesComponent.getInstance().setValue(IDE_RESTARTED_KEY, true)
      updateAction.actionPerformed(e)
    }
    else onSuccess()
  }

}