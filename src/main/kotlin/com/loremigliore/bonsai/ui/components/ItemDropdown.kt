package com.loremigliore.bonsai.ui.components

import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.popup.list.ListPopupImpl
import com.loremigliore.bonsai.BonsaiBundle
import com.loremigliore.bonsai.ui.theme.DrawUtils.applyListCellStyle
import java.awt.Component
import java.awt.Point
import javax.swing.JLabel
import javax.swing.ListCellRenderer
import javax.swing.SwingConstants

object ItemDropdown {
    enum class DropdownItem(
        val label: String,
    ) {
        FOCUS_SELECTED(BonsaiBundle.message("item.dropdown.focus_selected")),
        FOCUS_THIS(BonsaiBundle.message("item.dropdown.focus_this")),
        UNFOCUS(BonsaiBundle.message("item.dropdown.unfocus")),
        ADD_TO_SELECTION(BonsaiBundle.message("item.dropdown.add_select")),
        REMOVE_THIS_FROM_SELECTION(BonsaiBundle.message("item.dropdown.remove_this_select")),
        REMOVE_ALL_FROM_SELECTION(BonsaiBundle.message("item.dropdown.remove_all_select")),
        EXCLUDE_SELECTED(BonsaiBundle.message("item.dropdown.exclude_selected")),
        EXCLUDE_THIS(BonsaiBundle.message("item.dropdown.exclude_this")),
    }

    data class Context(
        val isFocused: Boolean,
        val isSelected: Boolean,
        val isAnySelected: Boolean,
        val isEmptyCanvas: Boolean,
    )

    fun show(
        component: Component,
        screenPoint: Point,
        context: Context,
        onFocus: () -> Unit = {},
        onUnfocus: () -> Unit = {},
        addToSelection: () -> Unit = {},
        removeFromSelection: () -> Unit = {},
        onExclude: () -> Unit = {},
    ) {
        val items =
            buildList {
                when {
                    context.isEmptyCanvas -> {
                        when {
                            context.isFocused -> add(DropdownItem.UNFOCUS)
                            context.isAnySelected -> add(DropdownItem.REMOVE_ALL_FROM_SELECTION)
                            else -> return
                        }
                    }

                    !context.isEmptyCanvas -> {
                        when {
                            context.isFocused -> {
                                add(DropdownItem.FOCUS_THIS)
                                add(DropdownItem.EXCLUDE_THIS)
                            }

                            context.isSelected -> {
                                add(DropdownItem.FOCUS_SELECTED)
                                add(DropdownItem.REMOVE_THIS_FROM_SELECTION)
                                add(DropdownItem.EXCLUDE_SELECTED)
                            }

                            context.isAnySelected -> {
                                add(DropdownItem.ADD_TO_SELECTION)
                            }

                            else -> {
                                add(DropdownItem.FOCUS_THIS)
                                add(DropdownItem.ADD_TO_SELECTION)
                                add(DropdownItem.EXCLUDE_THIS)
                            }
                        }
                    }
                }
            }

        val popup: ListPopup =
            JBPopupFactory.getInstance().createListPopup(
                object : BaseListPopupStep<DropdownItem>(null, items) {
                    override fun getTextFor(value: DropdownItem): String = value.label

                    override fun onChosen(
                        selectedValue: DropdownItem,
                        finalChoice: Boolean,
                    ): PopupStep<*>? =
                        doFinalStep {
                            when (selectedValue) {
                                DropdownItem.FOCUS_SELECTED, DropdownItem.FOCUS_THIS -> onFocus()
                                DropdownItem.UNFOCUS -> onUnfocus()
                                DropdownItem.ADD_TO_SELECTION -> addToSelection()
                                DropdownItem.REMOVE_THIS_FROM_SELECTION, DropdownItem.REMOVE_ALL_FROM_SELECTION -> removeFromSelection()
                                DropdownItem.EXCLUDE_SELECTED, DropdownItem.EXCLUDE_THIS -> onExclude()
                            }
                        }
                },
            )

        val popupImpl = popup as? ListPopupImpl ?: return
        popupImpl.list.cellRenderer =
            ListCellRenderer { list, value, _, isSelected, _ ->
                JLabel((value as DropdownItem).label, SwingConstants.LEADING).apply {
                    applyListCellStyle(list, isSelected)
                }
            }
        popup.show(RelativePoint(component, screenPoint))
    }
}
