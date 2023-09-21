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
package de.griefed.serverpackcreator.gui.window.settings.components

import de.griefed.serverpackcreator.api.ApiProperties
import java.awt.Dimension
import javax.swing.JFileChooser

/**
 * Customized filechooser for picking ServerPackCreator webservice-database directory.
 *
 * @author Griefed
 */
class WebserviceDBDirChooser(apiProperties: ApiProperties, title: String) : JFileChooser() {
    init {
        currentDirectory = apiProperties.serverPackCreatorDatabase.parentFile
        dialogTitle = title
        fileSelectionMode = DIRECTORIES_ONLY
        isAcceptAllFileFilterUsed = false
        isMultiSelectionEnabled = false
        dialogType = SAVE_DIALOG
        preferredSize = Dimension(750, 450)
    }
}