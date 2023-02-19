package de.griefed.serverpackcreator.gui.filebrowser.controller

import de.griefed.serverpackcreator.api.utilities.common.Utilities
import de.griefed.serverpackcreator.gui.filebrowser.model.FileNode
import de.griefed.serverpackcreator.gui.filebrowser.view.SelectionPopMenu
import de.griefed.serverpackcreator.gui.window.configs.ConfigsTab
import java.awt.event.MouseEvent
import java.io.File
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreePath

/**
 * TODO docs
 */
class TreeMouseListener(
    private val jTree: JTree, configsTab: ConfigsTab, utilities: Utilities
) : SelectionPopMenu(configsTab, utilities) {

    /**
     * TODO docs
     */
    override fun mousePressed(mouseEvent: MouseEvent) {
        if (mouseEvent.button == MouseEvent.BUTTON3) {
            if (jTree.getPathForLocation(mouseEvent.x, mouseEvent.y) != null) {
                val treePath = jTree.getPathForLocation(mouseEvent.x, mouseEvent.y)!!
                val treeNode = treePath.lastPathComponent as DefaultMutableTreeNode
                val fileNode = treeNode.userObject as FileNode
                val file = fileNode.file
                show(jTree, mouseEvent.x, mouseEvent.y, file)
            }
        }
    }
}