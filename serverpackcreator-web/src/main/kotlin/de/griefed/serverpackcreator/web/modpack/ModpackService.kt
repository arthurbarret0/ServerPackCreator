package de.griefed.serverpackcreator.web.modpack

import de.griefed.serverpackcreator.api.ApiProperties
import de.griefed.serverpackcreator.api.PackConfig
import de.griefed.serverpackcreator.web.data.ModPack
import de.griefed.serverpackcreator.web.data.ModPackView
import de.griefed.serverpackcreator.web.storage.DatabaseStorageService
import de.griefed.serverpackcreator.web.storage.StorageRepository
import de.griefed.serverpackcreator.web.storage.StorageSystem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

@Service
class ModpackService @Autowired constructor(
    private val modpackRepository: ModpackRepository,
    private val apiProperties: ApiProperties,
    private val storageRepository: StorageRepository
) {
    private val modPackStorageSystem: StorageSystem = StorageSystem(apiProperties.modpacksDirectory.toPath(), DatabaseStorageService(storageRepository), )

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
        modpack.clientMods = clientMods.ifBlank {
            apiProperties.clientSideMods().joinToString(",")
        }
        modpack.whiteListMods = whiteListMods.ifBlank {
            apiProperties.whitelistedMods().joinToString(",")
        }
        modpack.status = ModpackStatus.QUEUED
        modpack.name = file.originalFilename ?: file.name
        modpack.size = file.size.toDouble()
        modpack.source = ModpackSource.ZIP
        val storePair = modPackStorageSystem.store(file, System.currentTimeMillis().toInt()).get()
        modpack.fileID = storePair.first.toInt()
        modpack.fileHash = storePair.second.toBigInteger()
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
        packConfig.modpackDir = File(apiProperties.modpacksDirectory, "${modpack.fileID}.zip").absolutePath
        packConfig.setClientMods(modpack.clientMods.split(",").toMutableList())
        packConfig.setModsWhitelist(modpack.whiteListMods.split(",").toMutableList())
        packConfig.minecraftVersion = modpack.minecraftVersion
        packConfig.modloader = modpack.modloader
        packConfig.modloaderVersion = modpack.modloaderVersion
        packConfig.isZipCreationDesired = true
        return packConfig
    }

    fun deleteModpack(id: Int) {
        modpackRepository.deleteById(id)
    }

    /**
     * Store an uploaded ZIP-archive to disk.
     *
     * @param modpack The modpack to be stored to disk from the database
     * @return The path to the saved file.
     * @throws IOException If an I/O error occurs writing to or creating the file.
     * @throws IllegalArgumentException If the modpack doesn't have data to export.
     * @author Griefed
     */
    @Throws(IOException::class, IllegalArgumentException::class)
    fun exportModpackToDisk(modpack: ModPack): File {
        if (modPackStorageSystem.load(modpack.fileID.toString()).isEmpty) {
            throw IllegalArgumentException("Modpack ${modpack.id} does not have data to export.")
        }
        var zip = File(apiProperties.modpacksDirectory, "${modpack.fileID}.zip").absoluteFile
        // Does an archive with the same name already exist?
        if (zip.isFile) {
            var incrementation = 0
            val substring = zip.toString().substring(0, zip.toString().length - 4)
            while (File("${substring}_$incrementation.zip").isFile) {
                incrementation++
            }
            zip = Paths.get("${substring}_$incrementation.zip").toFile()
        }
        /*FileOutputStream(zipPath.toFile()).use {
            IOUtils.copy(modpack.data!!.binaryStream, it)
        }*/
        Files.write(zip, modpack.modPackFile!!.data!!)
        return zip
    }
}