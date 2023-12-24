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
package de.griefed.serverpackcreator.web.modpack

import de.griefed.serverpackcreator.api.ApiProperties
import de.griefed.serverpackcreator.api.ConfigurationHandler
import de.griefed.serverpackcreator.api.PackConfig
import de.griefed.serverpackcreator.web.customizing.ClientModRepository
import de.griefed.serverpackcreator.web.customizing.WhitelistedModRepository
import de.griefed.serverpackcreator.web.data.ClientMod
import de.griefed.serverpackcreator.web.data.ModPack
import de.griefed.serverpackcreator.web.data.ModPackView
import de.griefed.serverpackcreator.web.data.WhitelistedMod
import de.griefed.serverpackcreator.web.storage.StorageRepository
import de.griefed.serverpackcreator.web.storage.StorageSystem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Path
import java.util.*

@Service
class ModpackService @Autowired constructor(
    private val modpackRepository: ModpackRepository,
    private val apiProperties: ApiProperties,
    private val clientModRepository: ClientModRepository,
    private val whitelistedModRepository: WhitelistedModRepository,
    private val configurationHandler: ConfigurationHandler,
    storageRepository: StorageRepository,
) {
    private val rootLocation: Path = apiProperties.modpacksDirectory.toPath()
    private val storage: StorageSystem = StorageSystem(rootLocation, storageRepository)

    fun saveZipModpack(
        file: MultipartFile,
        minecraftVersion: String,
        modloader: String,
        modloaderVersion: String,
        clientMods: String,
        whiteListMods: String
    ): ModPack {
        val modpack = ModPack()
        modpack.minecraftVersion = minecraftVersion
        modpack.modloader = modloader
        modpack.modloaderVersion = modloaderVersion
        if (clientMods.isNotBlank()) {
            for (mod in clientMods.replace(", ",",").split(",")) {
                modpack.clientMods.add(ClientMod(mod))
            }
        } else {
            modpack.clientMods.addAll(apiProperties.clientSideMods().map { ClientMod(it) })
        }
        for (i in 0 until modpack.clientMods.size) {
            if (clientModRepository.findByMod(modpack.clientMods[i].mod).isPresent) {
                modpack.clientMods[i] = clientModRepository.findByMod(modpack.clientMods[i].mod).get()
            } else {
                modpack.clientMods[i] = clientModRepository.save(modpack.clientMods[i])
            }
        }
        if (whiteListMods.isNotBlank()) {
            for (mod in whiteListMods.replace(", ",",").split(",")) {
                modpack.whitelistedMods.add(WhitelistedMod(mod))
            }
        } else {
            modpack.whitelistedMods.addAll(apiProperties.whitelistedMods().map { WhitelistedMod(it) })
        }
        for (i in 0 until modpack.whitelistedMods.size) {
            if (whitelistedModRepository.findByMod(modpack.whitelistedMods[i].mod).isPresent) {
                modpack.whitelistedMods[i] = whitelistedModRepository.findByMod(modpack.whitelistedMods[i].mod).get()
            } else {
                modpack.whitelistedMods[i] = whitelistedModRepository.save(modpack.whitelistedMods[i])
            }
        }
        modpack.status = ModpackStatus.QUEUED
        modpack.name = file.originalFilename ?: file.name
        modpack.size = file.size.toDouble()
        modpack.source = ModpackSource.ZIP
        val storePair = storage.store(file).get()
        modpack.fileID = storePair.first
        modpack.fileHash = storePair.second
        return modpackRepository.save(modpack)
    }

    fun saveModpack(modpack: ModPack): ModPack {
        return modpackRepository.save(modpack)
    }

    fun getModpack(id: Int): Optional<ModPack> {
        return modpackRepository.findById(id)
    }

    fun getModpacks(): List<ModPackView> {
        return modpackRepository.findAllProjectedBy()
    }

    fun getPackConfigForModpack(modpack: ModPack): PackConfig {
        val packConfig = PackConfig()
        packConfig.modpackDir = rootLocation.resolve("${modpack.fileID}.zip").normalize().toFile().absolutePath
        packConfig.setClientMods(modpack.clientMods.map { it.mod }.toMutableList())
        packConfig.setModsWhitelist(modpack.whitelistedMods.map { it.mod }.toMutableList())
        if (modpack.status == ModpackStatus.GENERATING) {
            packConfig.inclusions.addAll(configurationHandler.suggestInclusions(packConfig.modpackDir))
        }
        packConfig.minecraftVersion = modpack.minecraftVersion
        packConfig.modloader = modpack.modloader
        packConfig.modloaderVersion = modpack.modloaderVersion
        packConfig.isZipCreationDesired = true
        return packConfig
    }

    fun deleteModpack(id: Int) {
        val modpack = modpackRepository.findById(id)
        if (modpack.isPresent) {
            modpackRepository.deleteById(id)
            if (modpack.get().fileID != null) {
                storage.delete(modpack.get().fileID!!)
            }
        }
    }

    /**
     * Store an uploaded ZIP-archive to disk.
     *
     * @param modPack The modpack to be stored to disk from the database
     * @return The modpack-file, wrapped in an [Optional]
     * @throws IOException If an I/O error occurs writing to or creating the file.
     * @throws IllegalArgumentException If the modpack doesn't have data to export.
     * @author Griefed
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    fun getModPackArchive(modPack: ModPack): Optional<File> {
        return storage.load(modPack.fileID!!)
    }

    fun getModPackArchive(id: Long): Optional<File> {
        return storage.load(id)
    }
}