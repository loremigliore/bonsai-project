package com.loremigliore.bonsai.logic.services.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.loremigliore.bonsai.domain.enums.TreeDetail
import com.loremigliore.bonsai.domain.enums.TreeLayout

@State(
    name = "com.loremigliore.bonsai.logic.services.settings.BonsaiSettings",
    storages = [Storage("bonsai_settings.xml")],
)
@Service
class BonsaiSettings : PersistentStateComponent<BonsaiSettings> {
    companion object {
        private val DEFAULT_EXCLUSIONS =
            listOf(
                "gradle",
                "examples",
            )

        private const val DEFAULT_HIDE_FOLDERS = false
        private const val DEFAULT_HIDE_FILES = false
        private const val DEFAULT_HIDE_MIDDLE_PACKAGES = false
        private const val DEFAULT_SHRINK_MIDDLE_PACKAGES = false
        private val DEFAULT_TREE_DETAIL = TreeDetail.BASE
        private val DEFAULT_TREE_LAYOUT = TreeLayout.CASCADE

        fun getInstance(): BonsaiSettings = ApplicationManager.getApplication().getService(BonsaiSettings::class.java)
    }

    var hideFolders: Boolean = DEFAULT_HIDE_FOLDERS
    var hideFiles: Boolean = DEFAULT_HIDE_FILES
    var hideMiddlePackages: Boolean = DEFAULT_HIDE_MIDDLE_PACKAGES
    var shrinkMiddlePackages: Boolean = DEFAULT_SHRINK_MIDDLE_PACKAGES

    var treeDetail: TreeDetail = DEFAULT_TREE_DETAIL
    var treeLayout: TreeLayout = DEFAULT_TREE_LAYOUT

    var defaultExclusions: List<String> = listOf()

    override fun getState(): BonsaiSettings = this

    override fun loadState(state: BonsaiSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    fun reset() {
        hideFolders = DEFAULT_HIDE_FOLDERS
        hideFiles = DEFAULT_HIDE_FILES
        hideMiddlePackages = DEFAULT_HIDE_MIDDLE_PACKAGES
        shrinkMiddlePackages = DEFAULT_SHRINK_MIDDLE_PACKAGES

        treeDetail = DEFAULT_TREE_DETAIL
        treeLayout = DEFAULT_TREE_LAYOUT

        defaultExclusions = DEFAULT_EXCLUSIONS
    }
}
