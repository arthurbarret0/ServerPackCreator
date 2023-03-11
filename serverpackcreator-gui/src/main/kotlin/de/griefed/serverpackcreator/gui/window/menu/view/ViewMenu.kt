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
package de.griefed.serverpackcreator.gui.window.menu.view

import Gui
import de.griefed.serverpackcreator.api.ApiProperties
import de.griefed.serverpackcreator.api.utilities.common.Utilities
import de.griefed.serverpackcreator.gui.window.MainFrame
import javax.swing.JMenu
import javax.swing.JSeparator

/**
 * Menu revolving around viewing directories and logs, mainly.
 *
 * @author Griefed
 */
class ViewMenu(utilities: Utilities,apiProperties: ApiProperties, mainFrame: MainFrame) : JMenu(Gui.menubar_gui_menu_view.toString()) {
    init {
        add(HomeDirItem(utilities.fileUtilities,apiProperties))
        add(ServerPacksDirItem(utilities.fileUtilities,apiProperties))
        add(ServerFilesDirItem(utilities.fileUtilities,apiProperties))
        add(ConfigsDirItem(utilities.fileUtilities,apiProperties))
        add(JSeparator())
        add(PluginsDirItem(utilities.fileUtilities,apiProperties))
        add(PluginsConfigDirItem(utilities.fileUtilities,apiProperties))
        add(JSeparator())
        add(MigrationInfoItem(mainFrame))
        add(JSeparator())
        add(ServerPackCreatorLogItem(utilities.fileUtilities,apiProperties))
        add(PluginsLogItem(utilities.fileUtilities,apiProperties))
        add(ModloaderInstallerLogItem(utilities.fileUtilities,apiProperties))
    }
}