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
package de.griefed.serverpackcreator.gui.window.configs.components

import java.awt.Font
import javax.swing.JLabel
import javax.swing.UIManager
import javax.swing.plaf.FontUIResource

/**
 * Label to display in front of various components in ServerPackCreator.
 *
 * @author Griefed
 */
class ElementLabel(text: String, private var size: Int = 0) : JLabel(text) {
    init {
        if (size == 0) {
            size = font.size
        }
        updateFont()
    }

    override fun updateUI() {
        super.updateUI()
        updateFont()
    }

    private fun updateFont() {
        val currentFont = UIManager.get("defaultFont") as Font
        font = FontUIResource(currentFont.fontName, Font.BOLD, currentFont.size)
    }
}