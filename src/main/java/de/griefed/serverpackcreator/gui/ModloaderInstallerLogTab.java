/* Copyright (C) 2021  Griefed
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
package de.griefed.serverpackcreator.gui;

import de.griefed.serverpackcreator.i18n.LocalizationManager;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * This class creates the tab which display the latest modloader_installer.log tailer.
 */
public class ModloaderInstallerLogTab extends JComponent {

    private LocalizationManager localizationManager;

    /**
     * <strong>Constructor</strong><p>
     * Used for Dependency Injection.<p>
     * Receives an instance of {@link LocalizationManager} or creates one if the received
     * one is null. Required for use of localization.
     * @param injectedLocalizationManager Instance of {@link LocalizationManager} required for localized log messages.
     */
    public ModloaderInstallerLogTab(LocalizationManager injectedLocalizationManager) {
        if (injectedLocalizationManager == null) {
            this.localizationManager = new LocalizationManager();
        } else {
            this.localizationManager = injectedLocalizationManager;
        }
    }

    private JComponent modloaderInstallerLogPanel;
    private GridBagConstraints constraints;
    private JTextArea textArea;
    private JScrollPane scrollPane;
    private SmartScroller smartScroller;

    /**
     * Create the tab for the modloader_installer.log tailer in a JScrollPane with an always available vertical scrollbar
     * and a horizontal scrollbar available as needed. Uses Apache commons-io's {@link Tailer} to keep the JTextArea up
     * to date with the latest log entries. Should any line contain "Starting Fabric installation." or
     * "Starting Forge installation." the textarea is cleared.
     * @return JComponent. Returns a JPanel containing a JScrollPane containing the JTextArea with the latest
     * modloader_installer.log entries.
     */
    JComponent modloaderInstallerLogTab() {
        modloaderInstallerLogPanel = new JPanel(false);
        modloaderInstallerLogPanel.setLayout(new GridBagLayout());
        constraints = new GridBagConstraints();

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weighty = 1;
        constraints.weightx = 1;

        //Log Panel
        textArea = new JTextArea();
        textArea.setEditable(false);

        Tailer.create(new File("./logs/modloader_installer.log"), new TailerListenerAdapter() {
            public void handle(String line) {
                synchronized (this) {
                    if (line.contains(localizationManager.getLocalizedString("serversetup.log.info.installserver.fabric.enter")) ||
                            line.contains(localizationManager.getLocalizedString("serversetup.log.info.installserver.forge.enter"))) {
                        textArea.setText("");
                    }
                    textArea.append(line + "\n");
                }
            }
        }, 2000, false);

        scrollPane = new JScrollPane(
                textArea,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        smartScroller = new SmartScroller(scrollPane);

        modloaderInstallerLogPanel.add(scrollPane, constraints);

        return modloaderInstallerLogPanel;
    }
}