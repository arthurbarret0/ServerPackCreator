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
package de.griefed.serverpackcreator.gui.window.configs

import Gui
import com.electronwill.nightconfig.core.CommentedConfig
import de.griefed.serverpackcreator.api.*
import de.griefed.serverpackcreator.api.plugins.swinggui.ServerPackConfigTab
import de.griefed.serverpackcreator.gui.GuiProps
import de.griefed.serverpackcreator.gui.components.*
import de.griefed.serverpackcreator.gui.window.configs.components.general.*
import de.griefed.serverpackcreator.gui.window.configs.components.specific.*
import kotlinx.coroutines.*
import net.miginfocom.swing.MigLayout
import org.apache.logging.log4j.kotlin.cachedLoggerOf
import java.awt.Dimension
import java.awt.event.ActionListener
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.event.DocumentEvent

/**
 * Panel to edit a server pack configuration. This panel contains any and all elements required to fully configure
 * a server pack to the users liking.
 *
 * @author Griefed
 */
class ConfigEditorPanel(
    private val guiProps: GuiProps,
    tabbedConfigsTab: TabbedConfigsTab,
    private val apiWrapper: ApiWrapper,
    private val noVersions: DefaultComboBoxModel<String>,
    showBrowser: ActionListener
) : JScrollPane(), ServerPackConfigTab {
    private val log = cachedLoggerOf(this.javaClass)
    private val modpackInfo = ModpackInfo(guiProps)
    private val propertiesInfo = PropertiesInfo(guiProps)
    private val iconInfo = IconInfo(guiProps)
    private val filesInfo = FilesInfo(guiProps)
    private val suffixInfo = SuffixInfo(guiProps)
    private val modloaderVersionInfo = ModloaderVersionInfo(guiProps)
    private val includeIconInfo = IncludeIconInfo(guiProps)
    private val includePropertiesInfo = IncludePropertiesInfo(guiProps)
    private val prepareServerInfo = PrepareServerInfo(guiProps)
    private val exclusionsInfo = ExclusionsInfo(guiProps)
    private val modpackInspect = IconActionButton(
        guiProps.inspectIcon,
        Gui.createserverpack_gui_buttonmodpackdir_scan_tip.toString()
    ) { updateGuiFromSelectedModpack() }
    private val includeIcon = ActionCheckBox(
        Gui.createserverpack_gui_createserverpack_checkboxicon.toString()
    ) { validateInputFields() }
    private val includeProperties = ActionCheckBox(
        Gui.createserverpack_gui_createserverpack_checkboxproperties.toString()
    ) { validateInputFields() }
    private val includeZip = ActionCheckBox(
        Gui.createserverpack_gui_createserverpack_checkboxzip.toString()
    ) { validateInputFields() }
    private val includeServer = ActionCheckBox(
        Gui.createserverpack_gui_createserverpack_checkboxserver.toString()
    ) { validateInputFields() }
    private val minecraftVersions = ActionComboBox(
        DefaultComboBoxModel(
            apiWrapper.versionMeta!!.minecraft.settingsDependantVersionsArrayDescending()
        )
    ) { updateMinecraftValues() }
    private val modloaders = ActionComboBox(
        DefaultComboBoxModel(
            apiWrapper.apiProperties.supportedModloaders
        )
    ) { updateMinecraftValues() }
    private val iconPreview = IconPreview(guiProps)
    private val javaVersion = ElementLabel("8", 16)
    private val legacyFabricVersions =
        DefaultComboBoxModel(apiWrapper.versionMeta!!.legacyFabric.loaderVersionsArrayDescending())
    private val fabricVersions = DefaultComboBoxModel(apiWrapper.versionMeta!!.fabric.loaderVersionsArrayDescending())
    private val quiltVersions = DefaultComboBoxModel(apiWrapper.versionMeta!!.quilt.loaderVersionsArrayDescending())
    private val modloaderVersions = ActionComboBox { validateInputFields() }
    private val aikarsFlags = AikarsFlags(this, guiProps)
    private val modpackDirectory = ScrollTextFileField("")
    private val scriptKVPairs = ScriptKVPairs(guiProps, this)
    private val pluginPanels = apiWrapper.apiPlugins!!.getConfigPanels(this).toMutableList()
    private var lastSavedConfig: PackConfig? = null
    private val changeListener = object : DocumentChangeListener {
        override fun update(e: DocumentEvent) {
            validateInputFields()
        }
    }
    private val javaArgs = ScrollTextArea("-Xmx4G -Xms4G", changeListener)
    private val serverPackSuffix = ScrollTextField("", changeListener)
    private val propertiesFile = ScrollTextFileField(apiWrapper.apiProperties.defaultServerProperties, changeListener)
    private val iconFile = ScrollTextFileField(apiWrapper.apiProperties.defaultServerIcon, changeListener)
    private val serverPackFiles = ScrollTextArea("config,mods", changeListener)
    private val exclusions = ScrollTextArea(apiWrapper.apiProperties.clientSideMods().joinToString(","), changeListener)
    private val timer = ConfigCheckTimer(250, this, guiProps)
    private val modpackChanges = object : DocumentChangeListener {
        override fun update(e: DocumentEvent) {
            title.title = modpackDirectory.file.name
            validateInputFields()
        }
    }
    private val panel = JPanel(
        MigLayout(
            "left,wrap",
            "[left,::64]5[left]5[left,grow]5[left,::64]5[left,::64]", "30"
        )
    )
    val propertiesQuickSelect = QuickSelect(apiWrapper.apiProperties.propertiesQuickSelections) { setProperties() }
    val iconQuickSelect = QuickSelect(apiWrapper.apiProperties.iconQuickSelections) { setIcon() }
    val title = ConfigEditorTitle(guiProps, tabbedConfigsTab, this)

    var configFile: File? = null
        private set

    init {
        viewport.view = panel
        verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED
        horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_AS_NEEDED
        verticalScrollBar.unitIncrement = 10


        minecraftVersions.selectedIndex = 0
        modloaders.selectedIndex = 0
        modpackDirectory.addDocumentListener(modpackChanges)

        updateMinecraftValues()

        // Modpack directory
        panel.add(modpackInfo, "cell 0 0,grow")
        panel.add(ElementLabel(Gui.createserverpack_gui_createserverpack_labelmodpackdir.toString()), "cell 1 0,grow")
        panel.add(modpackDirectory, "cell 2 0,grow")
        panel.add(
            IconActionButton(
                guiProps.folderIcon,
                Gui.createserverpack_gui_browser.toString(),
                showBrowser
            ),
            "cell 3 0, h 30!,w 30!"
        )
        panel.add(modpackInspect, "cell 4 0")

        // Server Properties
        panel.add(propertiesInfo, "cell 0 1,grow")
        panel.add(
            ElementLabel(Gui.createserverpack_gui_createserverpack_labelpropertiespath.toString()),
            "cell 1 1,grow"
        )
        panel.add(propertiesFile, "cell 2 1, split 3,grow, w 50:50:")
        panel.add(ElementLabel(Gui.createserverpack_gui_quickselect.toString()), "cell 2 1")
        panel.add(propertiesQuickSelect, "cell 2 1,w 200!")
        panel.add(
            IconActionButton(guiProps.folderIcon, Gui.createserverpack_gui_browser.toString(), showBrowser),
            "cell 3 1"
        )
        panel.add(
            IconActionButton(
                guiProps.openIcon,
                Gui.createserverpack_gui_createserverpack_button_open_properties.toString()
            ) { openServerProperties() },
            "cell 4 1"
        )

        // Server Icon
        panel.add(iconInfo, "cell 0 2,grow")
        panel.add(ElementLabel(Gui.createserverpack_gui_createserverpack_labeliconpath.toString()), "cell 1 2,grow")
        panel.add(iconFile, "cell 2 2, split 2,grow, w 50:50:")
        panel.add(ElementLabel(Gui.createserverpack_gui_quickselect.toString()), "cell 2 2")
        panel.add(iconQuickSelect, "cell 2 2,w 200!")
        panel.add(
            IconActionButton(guiProps.folderIcon, Gui.createserverpack_gui_browser.toString(), showBrowser),
            "cell 3 2"
        )
        panel.add(iconPreview, "cell 4 2")

        // Server Files
        panel.add(filesInfo, "cell 0 3 1 3")
        panel.add(ElementLabel(Gui.createserverpack_gui_createserverpack_labelcopydirs.toString()), "cell 1 3 1 3,grow")
        panel.add(serverPackFiles, "cell 2 3 1 3,grow,w 10:500:,h 100!")
        panel.add(
            IconActionButton(
                guiProps.revertIcon,
                Gui.createserverpack_gui_buttoncopydirs_revert_tip.toString()
            ) { revertServerPackFiles() },
            "cell 3 3 2 1, h 30!, aligny center, alignx center,growx"
        )
        panel.add(
            IconActionButton(guiProps.folderIcon, Gui.createserverpack_gui_browser.toString(), showBrowser),
            "cell 3 4 2 1, h 30!, aligny center, alignx center,growx"
        )
        panel.add(
            IconActionButton(
                guiProps.resetIcon,
                Gui.createserverpack_gui_buttoncopydirs_reset_tip.toString()
            ) { setCopyDirectories(apiWrapper.apiProperties.directoriesToInclude.toMutableList()) },
            "cell 3 5 2 1, h 30!, aligny top, alignx center,growx"
        )

        // Server Pack Suffix
        panel.add(suffixInfo, "cell 0 6,grow")
        panel.add(ElementLabel(Gui.createserverpack_gui_createserverpack_labelsuffix.toString()), "cell 1 6,grow")
        panel.add(serverPackSuffix, "cell 2 6,grow")

        // Minecraft Version
        panel.add(MinecraftVersionInfo(guiProps), "cell 0 7,grow")
        panel.add(ElementLabel(Gui.createserverpack_gui_createserverpack_labelminecraft.toString()), "cell 1 7,grow")
        panel.add(minecraftVersions, "cell 2 7,w 200!")
        // Java Version Of Minecraft Version
        panel.add(JavaVersionInfo(guiProps), "cell 2 7, w 40!, gapleft 40")
        panel.add(ElementLabel(Gui.createserverpack_gui_createserverpack_minecraft_java.toString(), 16), "cell 2 7")
        panel.add(javaVersion, "cell 2 7, w 40!")

        // Modloader
        panel.add(ModloaderInfo(guiProps), "cell 0 8,grow")
        panel.add(ElementLabel(Gui.createserverpack_gui_createserverpack_labelmodloader.toString()), "cell 1 8,grow")
        panel.add(modloaders, "cell 2 8,w 200!")
        // Include Server Icon
        panel.add(includeIconInfo, "cell 2 8, w 40!, gapleft 40,grow")
        panel.add(includeIcon, "cell 2 8, w 200!")
        //Create ZIP Archive
        panel.add(IncludeZipInfo(guiProps), "cell 2 8, w 40!,grow")
        panel.add(includeZip, "cell 2 8, w 200!")

        //Modloader Version
        panel.add(modloaderVersionInfo, "cell 0 9,grow")
        panel.add(
            ElementLabel(Gui.createserverpack_gui_createserverpack_labelmodloaderversion.toString()),
            "cell 1 9,grow"
        )
        panel.add(modloaderVersions, "cell 2 9,w 200!")
        //Include Server Properties
        panel.add(includePropertiesInfo, "cell 2 9, w 40!, gapleft 40,grow")
        panel.add(includeProperties, "cell 2 9, w 200!")
        //Install Local Server
        panel.add(prepareServerInfo, "cell 2 9, w 40!,grow")
        panel.add(includeServer, "cell 2 9, w 200!")

        // Advanced Settings
        panel.add(CollapsiblePanel(
            Gui.createserverpack_gui_advanced.toString(),
            AdvancedSettingsPanel(
                exclusionsInfo,
                JavaArgsInfo(guiProps),
                ScriptSettingsInfo(guiProps),
                exclusions,
                IconActionButton(
                    guiProps.revertIcon,
                    Gui.createserverpack_gui_buttonclientmods_revert_tip.toString()
                ) { revertExclusions() },
                IconActionButton(guiProps.folderIcon, Gui.createserverpack_gui_browser.toString(), showBrowser),
                IconActionButton(
                    guiProps.resetIcon,
                    Gui.createserverpack_gui_buttonclientmods_reset_tip.toString()
                ) { setClientSideMods(apiWrapper.apiProperties.clientSideMods()) },
                javaArgs,
                aikarsFlags,
                scriptKVPairs,
                IconActionButton(
                    guiProps.revertIcon,
                    Gui.createserverpack_gui_revert.toString()
                ) { revertScriptKVPairs() },
                IconActionButton(
                    guiProps.resetIcon,
                    Gui.createserverpack_gui_reset.toString()
                ) { resetScriptKVPairs() }
            )
        ), "cell 0 10 5,grow")

        // Plugins
        if (pluginPanels.isNotEmpty()) {
            panel.add(
                CollapsiblePanel(
                    Gui.createserverpack_gui_plugins.toString(),
                    PluginsSettingsPanel(pluginPanels)
                ), "cell 0 11 5,grow"
            )
        }
        validateInputFields()
        lastSavedConfig = getCurrentConfiguration()
    }

    override fun setClientSideMods(entries: MutableList<String>) {
        exclusions.text = apiWrapper.utilities!!.stringUtilities.buildString(entries)
        validateInputFields()
    }

    override fun setCopyDirectories(entries: MutableList<String>) {
        serverPackFiles.text = apiWrapper.utilities!!.stringUtilities.buildString(entries.toString())
        validateInputFields()
    }

    override fun setIconInclusionTicked(ticked: Boolean) {
        includeIcon.isSelected = ticked
    }

    override fun setJavaArguments(javaArguments: String) {
        javaArgs.text = javaArguments
    }

    override fun setMinecraftVersion(version: String) {
        for (i in 0 until minecraftVersions.model.size) {
            if (minecraftVersions.model.getElementAt(i) == version) {
                minecraftVersions.selectedIndex = i
                break
            }
        }
    }

    override fun setModloader(modloader: String) {
        when (modloader) {
            "Fabric" -> modloaders.selectedIndex = 0
            "Forge" -> modloaders.selectedIndex = 1
            "Quilt" -> modloaders.selectedIndex = 2
            "LegacyFabric" -> modloaders.selectedIndex = 3
        }
        setModloaderVersionsModel()
    }

    override fun setModloaderVersion(version: String) {
        for (i in 0 until modloaderVersions.model.size) {
            if (modloaderVersions.model.getElementAt(i) == version) {
                modloaderVersions.selectedIndex = i
                break
            }
        }
    }

    override fun setModpackDirectory(directory: String) {
        modpackDirectory.text = directory
    }

    override fun setPropertiesInclusionTicked(ticked: Boolean) {
        includeProperties.isSelected = ticked
    }

    override fun setScriptVariables(variables: HashMap<String, String>) {
        scriptKVPairs.loadData(variables)
    }

    override fun setServerIconPath(path: String) {
        iconFile.text = path
    }

    override fun setServerInstallationTicked(ticked: Boolean) {
        includeServer.isSelected = ticked
    }

    override fun setServerPackSuffix(suffix: String) {
        serverPackSuffix.text = apiWrapper.utilities!!.stringUtilities.pathSecureText(suffix)
    }

    override fun setServerPropertiesPath(path: String) {
        propertiesFile.text = path
    }

    override fun setZipArchiveCreationTicked(ticked: Boolean) {
        includeZip.isSelected = ticked
    }

    override fun getClientSideMods(): String {
        return exclusions.text.replace(", ", ",")
    }

    override fun getClientSideModsList(): MutableList<String> {
        return apiWrapper.utilities!!.listUtilities.cleanList(
            getClientSideMods().split(",")
                .dropLastWhile { it.isEmpty() }
                .toMutableList()
        )
    }

    override fun getCopyDirectories(): String {
        return serverPackFiles.text.replace(", ", ",")
    }

    override fun getCopyDirectoriesList(): MutableList<String> {
        return apiWrapper.utilities!!.listUtilities.cleanList(
            getCopyDirectories().split(",")
                .dropLastWhile { it.isEmpty() }
                .toMutableList()
        )
    }

    override fun getCurrentConfiguration(): PackConfig {
        return PackConfig(
            getClientSideModsList(),
            getCopyDirectoriesList(),
            getModpackDirectory(),
            getMinecraftVersion(),
            getModloader(),
            getModloaderVersion(),
            getJavaArguments(),
            getServerPackSuffix(),
            getServerIconPath(),
            getServerPropertiesPath(),
            isServerInstallationTicked(),
            isServerIconInclusionTicked(),
            isServerPropertiesInclusionTicked(),
            isZipArchiveCreationTicked(),
            getScriptSettings(),
            getExtensionsConfigs()
        )
    }

    override fun saveCurrentConfiguration(): File {
        val modpackName =
            apiWrapper.utilities!!.stringUtilities.pathSecureText(File(getModpackDirectory()).name + ".conf")
        val config = if (configFile != null) {
            configFile!!
        } else {
            File(apiWrapper.apiProperties.configsDirectory, modpackName)
        }
        lastSavedConfig = getCurrentConfiguration().save(config)
        configFile = config
        title.hideWarningIcon()
        return configFile!!
    }

    override fun getJavaArguments(): String {
        return javaArgs.text
    }

    override fun getMinecraftVersion(): String {
        return minecraftVersions.selectedItem!!.toString()
    }

    override fun getModloader(): String {
        return modloaders.selectedItem!!.toString()
    }

    override fun getModloaderVersion(): String {
        return modloaderVersions.selectedItem!!.toString()
    }

    override fun getModpackDirectory(): String {
        return modpackDirectory.text
    }

    override fun getScriptSettings(): HashMap<String, String> {
        return scriptKVPairs.getData()
    }

    override fun getServerIconPath(): String {
        return iconFile.text
    }

    /**
     * TODO docs
     */
    private fun setIcon() {
        if (iconQuickSelect.selectedIndex == 0) {
            return
        }
        val icon = iconQuickSelect.selectedItem
        if (icon != null && icon.toString() != Gui.createserverpack_gui_quickselect_choose.toString()) {
            setServerIconPath(File(apiWrapper.apiProperties.iconsDirectory, icon.toString()).absolutePath)
            iconQuickSelect.selectedIndex = 0
        }
    }

    /**
     * TODO docs
     */
    private fun setProperties() {
        if (propertiesQuickSelect.selectedIndex == 0) {
            return
        }
        val properties = propertiesQuickSelect.selectedItem
        if (properties != null && properties.toString() != Gui.createserverpack_gui_quickselect_choose.toString()) {
            setServerPropertiesPath(
                File(
                    apiWrapper.apiProperties.propertiesDirectory,
                    properties.toString()
                ).absolutePath
            )
            propertiesQuickSelect.selectedIndex = 0
        }
    }

    override fun getServerPackSuffix(): String {
        return apiWrapper.utilities!!.stringUtilities.pathSecureText(serverPackSuffix.text)
    }

    override fun getServerPropertiesPath(): String {
        return propertiesFile.text
    }

    override fun isMinecraftServerAvailable(): Boolean {
        return apiWrapper.versionMeta!!.minecraft.isServerAvailable(minecraftVersions.selectedItem!!.toString())
    }

    override fun isServerInstallationTicked(): Boolean {
        return includeServer.isSelected
    }

    override fun isServerIconInclusionTicked(): Boolean {
        return includeIcon.isSelected
    }

    override fun isServerPropertiesInclusionTicked(): Boolean {
        return includeProperties.isSelected
    }

    override fun isZipArchiveCreationTicked(): Boolean {
        return includeZip.isSelected
    }

    override fun clearScriptVariables() {
        scriptKVPairs.clearData()
    }

    override fun setAikarsFlagsAsJavaArguments() {
        if (getJavaArguments().isNotEmpty()) {
            when (JOptionPane.showConfirmDialog(
                this,
                Gui.createserverpack_gui_createserverpack_javaargs_confirm_message.toString(),
                Gui.createserverpack_gui_createserverpack_javaargs_confirm_title.toString(),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                guiProps.warningIcon
            )) {
                0 -> setJavaArguments(apiWrapper.apiProperties.aikarsFlags)
                1 -> {}
                else -> {}
            }
        } else {
            setJavaArguments(apiWrapper.apiProperties.aikarsFlags)
        }
    }

    override fun validateInputFields() {
        timer.restart()
    }

    override fun acquireRequiredJavaVersion(): String {
        return if (apiWrapper.versionMeta!!.minecraft.getServer(getMinecraftVersion()).isPresent
            && apiWrapper.versionMeta!!.minecraft.getServer(getMinecraftVersion()).get().javaVersion().isPresent
        ) {
            apiWrapper.versionMeta!!.minecraft.getServer(getMinecraftVersion()).get().javaVersion().get().toString()
        } else {
            "?"
        }
    }

    fun compareSettings() {
        if (lastSavedConfig == null) {
            title.showWarningIcon()
            return
        }

        val currentConfig = getCurrentConfiguration()

        when {
            currentConfig.clientMods != lastSavedConfig!!.clientMods
                    || currentConfig.copyDirs != lastSavedConfig!!.copyDirs
                    || currentConfig.javaArgs != lastSavedConfig!!.javaArgs
                    || currentConfig.minecraftVersion != lastSavedConfig!!.minecraftVersion
                    || currentConfig.modloader != lastSavedConfig!!.modloader
                    || currentConfig.modloaderVersion != lastSavedConfig!!.modloaderVersion
                    || currentConfig.modpackDir != lastSavedConfig!!.modpackDir
                    || currentConfig.scriptSettings != lastSavedConfig!!.scriptSettings
                    || currentConfig.serverIconPath != lastSavedConfig!!.serverIconPath
                    || currentConfig.serverPropertiesPath != lastSavedConfig!!.serverPropertiesPath
                    || currentConfig.serverPackSuffix != lastSavedConfig!!.serverPackSuffix
                    || currentConfig.isServerIconInclusionDesired != lastSavedConfig!!.isServerIconInclusionDesired
                    || currentConfig.isServerPropertiesInclusionDesired != lastSavedConfig!!.isServerPropertiesInclusionDesired
                    || currentConfig.isServerInstallationDesired != lastSavedConfig!!.isServerInstallationDesired
                    || currentConfig.isZipCreationDesired != lastSavedConfig!!.isZipCreationDesired -> {
                title.showWarningIcon()
            }

            else -> {
                title.hideWarningIcon()
            }
        }
    }

    /**
     * When the GUI has finished loading, try and load the existing serverpackcreator.conf-file into
     * ServerPackCreator.
     *
     * @author Griefed
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun loadConfiguration(packConfig: PackConfig, confFile: File) {
        GlobalScope.launch(guiProps.configDispatcher) {
            try {
                setModpackDirectory(packConfig.modpackDir)
                if (packConfig.clientMods.isEmpty()) {
                    setClientSideMods(apiWrapper.apiProperties.clientSideMods())
                    log.debug("Set clientMods with fallback list.")
                } else {
                    setClientSideMods(packConfig.clientMods)
                }
                if (packConfig.copyDirs.isEmpty()) {
                    setCopyDirectories(mutableListOf("mods", "config"))
                } else {
                    setCopyDirectories(packConfig.copyDirs)
                }
                setScriptVariables(packConfig.scriptSettings)
                setServerIconPath(packConfig.serverIconPath)
                setServerPropertiesPath(packConfig.serverPropertiesPath)
                if (packConfig.minecraftVersion.isEmpty()) {
                    packConfig.minecraftVersion = apiWrapper.versionMeta!!.minecraft.latestRelease().version
                }
                setMinecraftVersion(packConfig.minecraftVersion)
                setModloader(packConfig.modloader)
                setModloaderVersion(packConfig.modloaderVersion)
                setServerInstallationTicked(packConfig.isServerInstallationDesired)
                setIconInclusionTicked(packConfig.isServerIconInclusionDesired)
                setPropertiesInclusionTicked(packConfig.isServerPropertiesInclusionDesired)
                setZipArchiveCreationTicked(packConfig.isZipCreationDesired)
                setJavaArguments(packConfig.javaArgs)
                setServerPackSuffix(packConfig.serverPackSuffix)
                for (panel in pluginPanels) {
                    panel.setServerPackExtensionConfig(packConfig.getPluginConfigs(panel.pluginID))
                }
                lastSavedConfig = packConfig
                configFile = confFile
                title.hideWarningIcon()
            } catch (ex: Exception) {
                log.error("Couldn't load configuration file.", ex)
                JOptionPane.showMessageDialog(
                    this@ConfigEditorPanel,
                    Gui.createserverpack_gui_config_load_error_message.toString() + " " + ex.cause + "   ",
                    Gui.createserverpack_gui_config_load_error.toString(),
                    JOptionPane.ERROR_MESSAGE,
                    guiProps.errorIcon
                )
            }
        }
    }

    /**
     * Set the modloader version combobox model depending on the currently selected modloader, with
     * the specified Minecraft version.
     *
     * @param minecraftVersion The Minecraft version to work with in the GUI update.
     * @author Griefed
     */
    private fun setModloaderVersionsModel(minecraftVersion: String = minecraftVersions.selectedItem!!.toString()) {
        when (modloaders.selectedIndex) {
            0 -> updateFabricModel(minecraftVersion)
            1 -> updateForgeModel(minecraftVersion)
            2 -> updateQuiltModel(minecraftVersion)
            3 -> updateLegacyFabricModel(minecraftVersion)
            else -> {
                log.error("Invalid modloader selected.")
            }
        }
    }

    /**
     * TODO docs
     */
    private fun setModloaderVersions(
        model: DefaultComboBoxModel<String>,
        icon: Icon = guiProps.infoIcon,
        tooltip: String = Gui.createserverpack_gui_createserverpack_labelmodloaderversion_tip.toString()
    ) {
        modloaderVersionInfo.icon = icon
        modloaderVersionInfo.toolTipText = tooltip
        modloaderVersions.model = model
    }

    /**
     * TODO docs
     */
    private fun updateFabricModel(minecraftVersion: String = minecraftVersions.selectedItem!!.toString()) {
        if (apiWrapper.versionMeta!!.fabric.isMinecraftSupported(minecraftVersion)) {
            setModloaderVersions(fabricVersions)
        } else {
            setModloaderVersions(
                noVersions,
                guiProps.errorIcon,
                Gui.configuration_log_error_minecraft_modloader(getMinecraftVersion(), getModloader())
            )
        }
    }

    /**
     * TODO docs
     */
    private fun updateForgeModel(minecraftVersion: String = minecraftVersions.selectedItem!!.toString()) {
        if (apiWrapper.versionMeta!!.forge.supportedForgeVersionsDescendingArray(minecraftVersion).isPresent) {
            setModloaderVersions(
                DefaultComboBoxModel(
                    apiWrapper.versionMeta!!.forge.supportedForgeVersionsDescendingArray(minecraftVersion).get()
                )
            )
        } else {
            setModloaderVersions(
                noVersions,
                guiProps.errorIcon,
                Gui.configuration_log_error_minecraft_modloader(getMinecraftVersion(), getModloader())
            )
        }
    }

    /**
     * TODO docs
     */
    private fun updateQuiltModel(minecraftVersion: String = minecraftVersions.selectedItem!!.toString()) {
        if (apiWrapper.versionMeta!!.fabric.isMinecraftSupported(minecraftVersion)) {
            setModloaderVersions(quiltVersions)
        } else {
            setModloaderVersions(
                noVersions,
                guiProps.errorIcon,
                Gui.configuration_log_error_minecraft_modloader(getMinecraftVersion(), getModloader())
            )
        }
    }

    /**
     * TODO docs
     */
    private fun updateLegacyFabricModel(minecraftVersion: String = minecraftVersions.selectedItem!!.toString()) {
        if (apiWrapper.versionMeta!!.legacyFabric.isMinecraftSupported(minecraftVersion)) {
            setModloaderVersions(legacyFabricVersions)
        } else {
            setModloaderVersions(
                noVersions,
                guiProps.errorIcon,
                Gui.configuration_log_error_minecraft_modloader(getMinecraftVersion(), getModloader())
            )
        }
    }

    /**
     * Validate the input field for modpack directory.
     *
     * @author Griefed
     */
    fun validateModpackDir(): List<String> {
        val errors: MutableList<String> = ArrayList(20)
        if (apiWrapper.configurationHandler!!.checkModpackDir(getModpackDirectory(), errors, false)) {
            modpackInfo.info()
        } else {
            modpackInfo.error("<html>${errors.joinToString("<br>")}</html>")
        }
        for (error in errors) {
            log.error(error)
        }
        return errors
    }

    /**
     * Validate the input field for server pack suffix.
     *
     * @author Griefed
     */
    fun validateSuffix(): List<String> {
        val errors: MutableList<String> = ArrayList(10)
        if (apiWrapper.utilities!!.stringUtilities.checkForIllegalCharacters(serverPackSuffix.text)) {
            suffixInfo.info()
        } else {
            errors.add(Gui.configuration_log_error_serverpack_suffix.toString())
            suffixInfo.error("<html>${errors.joinToString("<br>")}</html>")
        }
        for (error in errors) {
            log.error(error)
        }
        return errors
    }

    /**
     * Validate the input field for client mods.
     *
     * @author Griefed
     */
    fun validateExclusions(): List<String> {
        val errors: MutableList<String> = ArrayList(10)
        if (!getClientSideMods().matches(guiProps.whitespace)) {
            exclusionsInfo.info()
        } else {
            errors.add(Gui.configuration_log_error_formatting.toString())
            exclusionsInfo.error("<html>${errors.joinToString("<br>")}</html>")
        }
        for (error in errors) {
            log.error(error)
        }
        return errors
    }

    /**
     * Validate the input field for copy directories.
     *
     * @author Griefed
     */
    fun validateServerPackFiles(): List<String> {
        val errors: MutableList<String> = ArrayList(10)
        apiWrapper.configurationHandler!!.checkCopyDirs(
            getCopyDirectoriesList(),
            getModpackDirectory(),
            errors,
            false
        )
        if (getCopyDirectories().matches(guiProps.whitespace)) {
            errors.add(Gui.configuration_log_error_formatting.toString())
        }
        if (errors.isNotEmpty()) {
            filesInfo.error("<html>${errors.joinToString("<br>")}</html>")
        } else {
            filesInfo.info()
        }
        for (error in errors) {
            log.error(error)
        }
        return errors
    }

    /**
     * Validate the input field for server icon.
     *
     * @author Griefed
     */
    fun validateServerIcon(): List<String> {
        val errors: MutableList<String> = ArrayList(10)
        if (getServerIconPath().isNotEmpty()) {
            if (apiWrapper.configurationHandler!!.checkIconAndProperties(getServerIconPath())) {
                iconInfo.info()
                includeIconInfo.info()
                setIconPreview(File(getServerIconPath()), errors)
            } else {
                iconInfo.error(Gui.configuration_log_error_servericon_error.toString())
                includeIconInfo.error(Gui.configuration_log_warn_icon.toString())
                iconPreview.updateIcon(guiProps.iconError)
            }
        } else {
            setIconPreview(apiWrapper.apiProperties.defaultServerIcon, errors)
            iconInfo.info()
            includeIconInfo.info()
        }
        for (error in errors) {
            log.error(error)
        }
        return errors
    }

    /**
     * Validate the inputfield for server properties.
     *
     * @author Griefed
     */
    fun validateServerProperties(): List<String> {
        val errors: MutableList<String> = ArrayList(10)
        if (apiWrapper.configurationHandler!!.checkIconAndProperties(getServerPropertiesPath())) {
            propertiesInfo.info()
            includePropertiesInfo.info()
        } else {
            propertiesInfo.error(Gui.configuration_log_warn_properties.toString())
            includePropertiesInfo.error(Gui.configuration_log_warn_properties.toString())
        }
        for (error in errors) {
            log.error(error)
        }
        return errors
    }

    /**
     * TODO docs
     */
    private fun setIconPreview(icon: File, errors: MutableList<String>) {
        try {
            iconPreview.updateIcon(ImageIcon(ImageIO.read(icon)))
        } catch (ex: IOException) {
            log.error("Error generating server icon preview.", ex)
            errors.add(Gui.configuration_log_error_servericon_error.toString())
        }
    }

    /**
     * Get the configurations of the available ExtensionConfigPanel as a hashmap, so we can store them
     * in our serverpackcreator.conf.
     *
     * @return Map containing lists of CommentedConfigs mapped to the corresponding pluginID.
     */
    private fun getExtensionsConfigs(): HashMap<String, ArrayList<CommentedConfig>> {
        val configs: HashMap<String, ArrayList<CommentedConfig>> = HashMap(10)
        if (pluginPanels.isNotEmpty()) {
            for (panel in pluginPanels) {
                configs[panel.pluginID] = panel.serverPackExtensionConfig()
            }
        }
        return configs
    }

    /**
     * Scan the modpack directory for various manifests and, if any are found, parse them and try to
     * load the Minecraft version, modloader and modloader version.
     *
     * @author Griefed
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun updateGuiFromSelectedModpack() {
        GlobalScope.launch(guiProps.configDispatcher) {
            modpackInspect.isEnabled = false
            if (File(getModpackDirectory()).isDirectory) {
                try {
                    val packConfig = PackConfig()
                    var updated = false
                    if (File(getModpackDirectory(), "manifest.json").isFile) {
                        apiWrapper.configurationHandler!!.updateConfigModelFromCurseManifest(
                            packConfig, File(
                                getModpackDirectory(), "manifest.json"
                            )
                        )
                        updated = true
                    } else if (File(getModpackDirectory(), "minecraftinstance.json").isFile) {
                        apiWrapper.configurationHandler!!.updateConfigModelFromMinecraftInstance(
                            packConfig, File(
                                getModpackDirectory(), "minecraftinstance.json"
                            )
                        )
                        updated = true
                    } else if (File(getModpackDirectory(), "modrinth.index.json").isFile) {
                        apiWrapper.configurationHandler!!.updateConfigModelFromModrinthManifest(
                            packConfig, File(
                                getModpackDirectory(), "modrinth.index.json"
                            )
                        )
                        updated = true
                    } else if (File(getModpackDirectory(), "instance.json").isFile) {
                        apiWrapper.configurationHandler!!.updateConfigModelFromATLauncherInstance(
                            packConfig, File(
                                getModpackDirectory(), "instance.json"
                            )
                        )
                        updated = true
                    } else if (File(getModpackDirectory(), "config.json").isFile) {
                        apiWrapper.configurationHandler!!.updateConfigModelFromConfigJson(
                            packConfig, File(
                                getModpackDirectory(), "config.json"
                            )
                        )
                        updated = true
                    } else if (File(getModpackDirectory(), "mmc-pack.json").isFile) {
                        apiWrapper.configurationHandler!!.updateConfigModelFromMMCPack(
                            packConfig, File(
                                getModpackDirectory(), "mmc-pack.json"
                            )
                        )
                        updated = true
                    }
                    val dirsToInclude = TreeSet(getCopyDirectoriesList())
                    val files = File(getModpackDirectory()).listFiles()
                    if (files != null && files.isNotEmpty()) {
                        for (file in files) {
                            if (apiWrapper.apiProperties.directoriesToInclude.contains(file.name)) {
                                dirsToInclude.add(file.name)
                            }
                        }
                    }
                    if (updated) {
                        setMinecraftVersion(packConfig.minecraftVersion)
                        setModloader(packConfig.modloader)
                        setModloaderVersion(packConfig.modloaderVersion)
                        setCopyDirectories(ArrayList(dirsToInclude))
                        JOptionPane.showMessageDialog(
                            this@ConfigEditorPanel,
                            Gui.createserverpack_gui_modpack_scan_message(
                                getMinecraftVersion(),
                                getModloader(),
                                getModloaderVersion(),
                                apiWrapper.utilities!!.stringUtilities.buildString(dirsToInclude.toList())
                            ) + "   ",
                            Gui.createserverpack_gui_modpack_scan.toString(),
                            JOptionPane.INFORMATION_MESSAGE,
                            guiProps.infoIcon
                        )
                    }
                } catch (ex: IOException) {
                    log.error("Couldn't update GUI from modpack manifests.", ex)
                }
            }
            modpackInspect.isEnabled = true
        }
    }

    /**
     * TODO docs
     */
    private fun revertScriptKVPairs() {
        if (lastSavedConfig != null) {
            setScriptVariables(lastSavedConfig!!.scriptSettings)
        }
    }

    /**
     * TODO docs
     */
    private fun resetScriptKVPairs() {
        setScriptVariables(guiProps.defaultScriptKVSetting)
    }

    /**
     * Reverts the list of clientside-only mods to the value of the last loaded configuration, if one
     * is available.
     *
     * @author Griefed
     */
    private fun revertExclusions() {
        if (lastSavedConfig != null) {
            setClientSideMods(lastSavedConfig!!.clientMods)
            validateInputFields()
        }
    }

    /**
     * Reverts the list of copy directories to the value of the last loaded configuration, if one is
     * available.
     *
     * @author Griefed
     */
    private fun revertServerPackFiles() {
        if (lastSavedConfig != null) {
            setCopyDirectories(lastSavedConfig!!.copyDirs)
            validateInputFields()
        }
    }

    /**
     * Setter for the Minecraft version depending on which one is selected in the GUI.
     *
     * @author Griefed
     */
    private fun updateMinecraftValues() {
        setModloaderVersionsModel(minecraftVersions.selectedItem!!.toString())
        updateRequiredJavaVersion()
        checkMinecraftServer()
        validateInputFields()
    }

    /**
     * TODO docs
     */
    private fun updateRequiredJavaVersion() {
        javaVersion.text = acquireRequiredJavaVersion()
        if (javaVersion.text != "?"
            && apiWrapper.apiProperties.javaPath(javaVersion.text).isPresent
            && !scriptKVPairs.getData()["SPC_JAVA_SPC"].equals(
                apiWrapper.apiProperties.javaPath(javaVersion.text).get()
            )
            && apiWrapper.apiProperties.isJavaScriptAutoupdateEnabled
        ) {
            val path = apiWrapper.apiProperties.javaPath(javaVersion.text).get()
            val data: HashMap<String, String> = scriptKVPairs.getData()
            data.replace(
                "SPC_JAVA_SPC", path
            )
            scriptKVPairs.loadData(data)
            log.info("Automatically adjusted script variable SPC_JAVA_SPC to $path")
        }
    }

    /**
     * Check whether the selected Minecraft version has a server available. If no server is available,
     * or no URL to download the server for the selected version is available, a warning is
     * displayed.
     *
     * @author Griefed
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun checkMinecraftServer() {
        val mcVersion = minecraftVersions.selectedItem?.toString()
        val server = apiWrapper.versionMeta!!.minecraft.getServer(mcVersion!!)
        if (!server.isPresent) {
            prepareServerInfo.warning(Gui.configuration_log_warn_server.toString())
            JOptionPane.showMessageDialog(
                this,
                Gui.createserverpack_gui_createserverpack_minecraft_server_unavailable(mcVersion) + "   ",
                Gui.createserverpack_gui_createserverpack_minecraft_server.toString(),
                JOptionPane.WARNING_MESSAGE,
                guiProps.warningIcon
            )
        } else if (server.isPresent && !server.get().url().isPresent) {
            prepareServerInfo.warning(Gui.configuration_log_warn_server.toString())
            JOptionPane.showMessageDialog(
                this,
                Gui.createserverpack_gui_createserverpack_minecraft_server_url_unavailable(mcVersion) + "   ",
                Gui.createserverpack_gui_createserverpack_minecraft_server.toString(),
                JOptionPane.WARNING_MESSAGE,
                guiProps.warningIcon
            )
        } else {
            prepareServerInfo.info()
        }
    }

    /**
     * If the checkbox is ticked and no Java is available, a message is displayed, warning the user
     * that Javapath needs to be defined for the modloader-server installation to work. If "Yes" is
     * clicked, a filechooser will open where the user can select their Java-executable/binary. If
     * "No" is selected, the user is warned about the consequences of not setting the
     * Javapath.<br></br><br></br>
     *
     *
     * If the installer for the current combination of the aforementioned versions can not be reached,
     * or is otherwise unavailable, the user is informed about SPC not being able to install it, thus
     * clearing the selection of the checkbox.
     *
     * Whenever the state of the server-installation checkbox changes, the global Java setting is
     * checked for validity, as well as the current combination of Minecraft version, modloader and
     * modloader version server-installer is available.<br></br><br></br>
     *
     *
     * If the checkbox is ticked and no Java is available, a message is displayed, warning the user
     * that Javapath needs to be defined for the modloader-server installation to work. If "Yes" is
     * clicked, a filechooser will open where the user can select their Java-executable/binary. If
     * "No" is selected, the user is warned about the consequences of not setting the
     * Javapath.<br></br><br></br>
     *
     *
     * If the installer for the current combination of the aforementioned versions can not be reached,
     * or is otherwise unavailable, the user is informed about SPC not being able to install it, thus
     * clearing the selection of the checkbox.
     *
     * @return `true` if, and only if, no problem was encountered.
     * @author Griefed
     */
    fun checkServer(): Boolean {
        var okay = true
        if (isServerInstallationTicked()) {
            val mcVersion = minecraftVersions.selectedItem!!.toString()
            val modloader = modloaders.selectedItem!!.toString()
            val modloaderVersion = modloaderVersions.selectedItem!!.toString()
            if (!checkJava()) {
                setServerInstallationTicked(false)
                okay = false
            }
            if (!apiWrapper.serverPackHandler!!.serverDownloadable(mcVersion, modloader, modloaderVersion)) {
                val message = Gui.createserverpack_gui_createserverpack_checkboxserver_unavailable_message(
                    modloader,
                    mcVersion,
                    modloader,
                    modloaderVersion,
                    modloader
                ) + "    "
                val title = Gui.createserverpack_gui_createserverpack_checkboxserver_unavailable_title(
                    mcVersion,
                    modloader,
                    modloaderVersion
                )
                JOptionPane.showMessageDialog(
                    this.panel.parent.parent,
                    message,
                    title,
                    JOptionPane.WARNING_MESSAGE,
                    guiProps.largeWarningIcon
                )
                setServerInstallationTicked(false)
                okay = false
            }
        }
        return okay
    }

    /**
     * If no Java is available, a message is displayed, warning the user that Javapath needs to be
     * defined for the modloader-server installation to work. If "Yes" is clicked, a filechooser will
     * open where the user can select their Java-executable/binary. If "No" is selected, the user is
     * warned about the consequences of not setting the Javapath.
     *
     * @return `true` if Java is available or was configured by the user.
     * @author Griefed
     */
    private fun checkJava(): Boolean {
        return if (!apiWrapper.apiProperties.javaAvailable()) {
            when (JOptionPane.showConfirmDialog(
                this,
                Gui.createserverpack_gui_createserverpack_checkboxserver_confirm_message.toString(),
                Gui.createserverpack_gui_createserverpack_checkboxserver_confirm_title.toString(),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                guiProps.warningIcon
            )) {
                0 -> {
                    chooseJava()
                    true
                }

                1 -> {
                    JOptionPane.showMessageDialog(
                        this,
                        Gui.createserverpack_gui_createserverpack_checkboxserver_message_message.toString(),
                        Gui.createserverpack_gui_createserverpack_checkboxserver_message_title.toString(),
                        JOptionPane.ERROR_MESSAGE,
                        guiProps.errorIcon
                    )
                    false
                }

                else -> false
            }
        } else {
            true
        }
    }

    /**
     * Opens a filechooser to select the Java-executable/binary.
     *
     * @author Griefed
     */
    private fun chooseJava() {
        val javaChooser = JFileChooser()
        if (File("%s/bin/".format(System.getProperty("java.home"))).isDirectory) {
            javaChooser.currentDirectory = File("%s/bin/".format(System.getProperty("java.home")))
        } else {
            javaChooser.currentDirectory = apiWrapper.apiProperties.homeDirectory
        }
        javaChooser.dialogTitle = Gui.createserverpack_gui_buttonjavapath_tile.toString()
        javaChooser.fileSelectionMode = JFileChooser.FILES_ONLY
        javaChooser.isAcceptAllFileFilterUsed = true
        javaChooser.isMultiSelectionEnabled = false
        javaChooser.preferredSize = Dimension(750, 450)
        if (javaChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            apiWrapper.apiProperties.javaPath = javaChooser.selectedFile.path
            log.debug("Set path to Java executable to: ${javaChooser.selectedFile.path}")
        }
    }

    /**
     * Open the server.properties-file in the local editor. If no file is specified by the user, the
     * default server.properties in `server_files` will be opened.
     *
     * @author Griefed
     */
    private fun openServerProperties() {
        if (File(getServerPropertiesPath()).isFile) {
            apiWrapper.utilities!!.fileUtilities.openFile(getServerPropertiesPath())
        } else {
            apiWrapper.utilities!!.fileUtilities.openFile(apiWrapper.apiProperties.defaultServerProperties)
        }
    }

    fun hasUnsavedChanges(): Boolean {
        return title.hasUnsavedChanges
    }
}
