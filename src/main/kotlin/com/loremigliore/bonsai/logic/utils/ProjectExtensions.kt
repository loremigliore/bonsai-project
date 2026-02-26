package com.loremigliore.bonsai.logic.utils

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.loremigliore.bonsai.logic.services.actions.CanvasAction
import com.loremigliore.bonsai.logic.services.actions.CanvasToolbarAction
import com.loremigliore.bonsai.logic.services.actions.HeaderAction
import com.loremigliore.bonsai.logic.services.actions.SearchAction
import com.loremigliore.bonsai.logic.services.actions.ToolbarAction
import com.loremigliore.bonsai.logic.services.settings.ProjectSettings
import com.loremigliore.bonsai.logic.services.stateHolders.CacheStateHolder
import com.loremigliore.bonsai.logic.services.stateHolders.VisualizerStateHolder
import java.awt.Component

fun Component.requireProject(): Project =
    DataManager
        .getInstance()
        .getDataContext(this)
        .getData(CommonDataKeys.PROJECT)
        ?: error("Project not found in DataContext. Component must be attached to a tool window.")

val Project.toolbarAction: ToolbarAction get() = getService(ToolbarAction::class.java)
val Project.headerAction: HeaderAction get() = getService(HeaderAction::class.java)

val Project.canvasAction: CanvasAction get() = getService(CanvasAction::class.java)

val Project.canvasToolbarAction: CanvasToolbarAction get() = getService(CanvasToolbarAction::class.java)

val Project.searchAction: SearchAction get() = getService(SearchAction::class.java)

val Project.projectSettings: ProjectSettings get() = getService(ProjectSettings::class.java)

val Project.visualizerState: VisualizerStateHolder get() = getService(VisualizerStateHolder::class.java)
val Project.cacheState: CacheStateHolder get() = getService(CacheStateHolder::class.java)
