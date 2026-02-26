package com.loremigliore.bonsai.domain.states

data class ToolbarState(
    val hideFolders: Boolean,
    val hideFiles: Boolean,
    val hideMiddlePackages: Boolean,
    val shrinkMiddlePackages: Boolean,
)
