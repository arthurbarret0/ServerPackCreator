/* Copyright (C) 2023  Griefed
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 *
 * The full license can be found at https:github.com/Griefed/ServerPackCreator/blob/main/LICENSE
 */
package de.griefed.serverpackcreator.gui.window.configs.filebrowser.view

import Gui
import de.griefed.serverpackcreator.api.utilities.common.Utilities
import de.griefed.serverpackcreator.gui.window.configs.TabbedConfigsTab
import de.griefed.serverpackcreator.gui.window.configs.filebrowser.controller.TableSelectionListener
import de.griefed.serverpackcreator.gui.window.configs.filebrowser.model.FileBrowserModel
import de.griefed.serverpackcreator.gui.window.configs.filebrowser.model.FileNode
import de.griefed.serverpackcreator.gui.window.configs.filebrowser.model.FileTableModel
import java.awt.BorderLayout
import java.awt.Dimension
import java.io.File
import java.text.NumberFormat
import java.util.*
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode

/**
 * Scroll-pane housing the table for files inside a selected directory.
 *
 * @author Griefed (Kotlin Conversion and minor changes)
 * @author Andrew Thompson
 * @see <a href="https://codereview.stackexchange.com/questions/4446/file-browser-gui">File Browser GUI</a>
 * @license LGPL
 */
class TableScrollPane(
    private val browserModel: FileBrowserModel,
    tabbedConfigsTab: TabbedConfigsTab,
    private val utilities: Utilities,
    fileDetailPanel: FileDetailPanel,
    filePreviewPanel: FilePreviewPanel
) {
    private var ftModel: FileTableModel
    private var countLabel: JLabel
    private var tsListener: TableSelectionListener
    private var scrollPane: JScrollPane
    var panel: JPanel = JPanel()

    init {
        panel.layout = BorderLayout()

        val countPanel = JPanel()
        countLabel = JLabel(" ")
        countPanel.add(countLabel)
        panel.add(countPanel, BorderLayout.NORTH)

        ftModel = FileTableModel()
        val table = JTable(ftModel)
        table.autoCreateRowSorter = true
        table.autoResizeMode = JTable.AUTO_RESIZE_OFF
        table.columnSelectionAllowed = false
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.addMouseListener(
            de.griefed.serverpackcreator.gui.window.configs.filebrowser.controller.TableMouseListener(
                table,
                tabbedConfigsTab,
                utilities
            )
        )

        tsListener = de.griefed.serverpackcreator.gui.window.configs.filebrowser.controller.TableSelectionListener(
            table,
            browserModel,
            fileDetailPanel,
            filePreviewPanel
        )
        tsListener.setRowCount(ftModel.rowCount)

        val lsm: ListSelectionModel = table.selectionModel
        lsm.addListSelectionListener(tsListener)
        val width: Int = ftModel.setColumnWidths(table)
        table.preferredScrollableViewportSize = Dimension(width, table.rowHeight * 12)
        scrollPane = JScrollPane(table)
        panel.add(scrollPane, BorderLayout.CENTER)
    }

    /**
     * Build the label for a node.
     *
     * @author Griefed
     */
    private fun buildLabelString(count: Int): String {
        val nf: NumberFormat = NumberFormat.getInstance()
        return (nf.format(count.toLong()) + " " + Gui.filebrowser_table_header)
    }

    /**
     * Clear the tree model.
     *
     * @author Griefed (Kotlin Conversion and minor changes)
     * @author Andrew Thompson
     */
    fun clearDefaultTableModel() {
        ftModel.removeRows()
        countLabel.text = " "
        ftModel.fireTableDataChanged()
    }

    /**
     * Update the table with the given node, updating all entries and information.
     *
     * @author Griefed (Kotlin Conversion and minor changes)
     * @author Andrew Thompson
     */
    fun setDefaultTableModel(node: DefaultMutableTreeNode) {
        ftModel.removeRows()
        val fileNode: FileNode = node.userObject as FileNode
        val resolved = File(utilities.fileUtilities.resolveLink(fileNode.file))
        var childNode: FileNode
        if (resolved.isDirectory) {
            val files = resolved.listFiles()
            for (file in files) {
                childNode = FileNode(file)
                ftModel.addRow(browserModel,childNode)
            }
        }
        tsListener.setRowCount(ftModel.rowCount)
        countLabel.text = buildLabelString(ftModel.rowCount)
        ftModel.fireTableDataChanged()
        scrollPane.verticalScrollBar.value = 0
    }
}