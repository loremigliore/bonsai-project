package com.loremigliore.bonsai.logic.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.loremigliore.bonsai.logic.utils.cacheState

class ProjectStructureStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        project.cacheState.structureChanged()

        project.messageBus.connect().subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: List<VFileEvent>) {
                    project.cacheState.structureChanged()
                }
            },
        )
    }
}
