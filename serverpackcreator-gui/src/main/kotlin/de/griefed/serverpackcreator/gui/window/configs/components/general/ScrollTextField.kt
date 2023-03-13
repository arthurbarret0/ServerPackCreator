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
package de.griefed.serverpackcreator.gui.window.configs.components.general

import java.awt.Toolkit
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.BorderFactory
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.event.DocumentListener
import javax.swing.event.UndoableEditEvent
import javax.swing.event.UndoableEditListener
import javax.swing.undo.CannotRedoException
import javax.swing.undo.CannotUndoException
import javax.swing.undo.UndoManager

/**
 * Scrollable textfield with an [UndoManager] providing up to ten undos. By default, the horizontal scrollbar is
 * displayed as needed.
 *
 * @author Griefed
 */
open class ScrollTextField(
    text: String,
    private val textField: JTextField = JTextField(text),
    horizontalScrollbarVisibility: Int = HORIZONTAL_SCROLLBAR_AS_NEEDED
) : JScrollPane(VERTICAL_SCROLLBAR_NEVER, horizontalScrollbarVisibility),
    UndoableEditListener,
    FocusListener,
    KeyListener {

    constructor(text: String, documentChangeListener: DocumentChangeListener) : this(text) {
        addDocumentListener(documentChangeListener)
    }

    private val undoManager = UndoManager()

    var text: String
        get() {
            return textField.text
        }
        set(value) {
            textField.text = value
        }

    init {
        undoManager.limit = 10
        textField.text = text
        textField.border = BorderFactory.createEmptyBorder(0, 5, 0, 5)
        textField.document.addUndoableEditListener(this)
        textField.addKeyListener(this)
        textField.addFocusListener(this)
        viewport.view = textField

    }

    fun addDocumentListener(listener: DocumentListener) {
        textField.document.addDocumentListener(listener)
    }

    override fun undoableEditHappened(e: UndoableEditEvent) {
        undoManager.addEdit(e.edit)
    }

    override fun focusGained(e: FocusEvent) {}

    override fun focusLost(e: FocusEvent) {}

    override fun keyTyped(e: KeyEvent) {}

    override fun keyPressed(e: KeyEvent) {
        when {
            e.keyCode == KeyEvent.VK_Z && e.isControlDown -> {
                try {
                    undoManager.undo()
                } catch (cue : CannotUndoException) {
                    Toolkit.getDefaultToolkit().beep()
                }
            }
            e.keyCode == KeyEvent.VK_Y && e.isControlDown -> {
                try {
                    undoManager.redo()
                } catch (cue : CannotRedoException) {
                    Toolkit.getDefaultToolkit().beep()
                }
            }
        }
    }

    override fun keyReleased(e: KeyEvent) {}
}