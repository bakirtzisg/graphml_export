package net.bakirtzis.exporter

import com.nomagic.actions.AMConfigurator
import com.nomagic.actions.ActionsCategory
import com.nomagic.actions.ActionsManager
import com.nomagic.actions.NMAction
import com.nomagic.magicdraw.actions.MDActionsCategory


class MainMenuConfigurator implements AMConfigurator {
    String GRAPHML_EXPORT_MENU_CATEGORY = "GraphML Export"
    String GRAPHML_EXPORT_MENU_ID = "graphml_export"

    private NMAction action

    def MainMenuConfigurator(NMAction action) {
        this.action = action
    }

    void configure(ActionsManager manager) {
        ActionsCategory category = (ActionsCategory) manager.getActionFor(GRAPHML_EXPORT_MENU_ID)

        if (category == null) {
            category = new MDActionsCategory(GRAPHML_EXPORT_MENU_ID, GRAPHML_EXPORT_MENU_CATEGORY)
            category.setNested(true)
            manager.addCategory(category)
        }

        category.addAction(action)
    }

    int getPriority() {
        return AMConfigurator.MEDIUM_PRIORITY;
    }
}