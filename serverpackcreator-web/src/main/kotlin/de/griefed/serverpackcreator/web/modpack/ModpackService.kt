package de.griefed.serverpackcreator.web.modpack

import de.griefed.serverpackcreator.api.ApiProperties
import de.griefed.serverpackcreator.api.PackConfig
import de.griefed.serverpackcreator.web.data.FileData
import de.griefed.serverpackcreator.web.data.ModPack
import de.griefed.serverpackcreator.web.data.ModPackView
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

@Service
class ModpackService @Autowired constructor(
    private val modpackRepository: ModpackRepository,
    private val apiProperties: ApiProperties
) {

    fun saveZipModpack(
        file: MultipartFile,
        minecraftVersion: String,
        modloader: String,
        modloaderVersion: String,
        clientMods: String,
        whiteListMods: String
    ): ModPack {
        val modpack = ModPack()
        modpack.name = file.originalFilename?.replace(".zip", "", ignoreCase = true) ?: file.name
        modpack.file = file.originalFilename ?: file.name
        modpack.size = file.size.div(1048576.0)
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
        modpack.source = ModpackSource.ZIP
        modpack.fileData = FileData()
        modpack.fileData!!.data = file.bytes
        return modpackRepository.save(modpack)
    }

    fun saveModpack(modpack: ModPack): ModPack {
        val dbEntry = modpackRepository.findById(modpack.id)
        val dbModPack: ModPack = if (dbEntry.isPresent) {
            dbEntry.get()
        } else {
            ModPack()
        }
        dbModPack.name = modpack.name
        dbModPack.projectID = modpack.projectID
        dbModPack.versionID = modpack.versionID
        dbModPack.minecraftVersion = modpack.minecraftVersion
        dbModPack.modloader = modpack.modloader
        dbModPack.modloaderVersion = modpack.modloaderVersion
        dbModPack.clientMods = modpack.clientMods
        dbModPack.whiteListMods = modpack.whiteListMods
        dbModPack.file = modpack.file
        dbModPack.size = modpack.size
        dbModPack.status = modpack.status
        dbModPack.source = modpack.source
        dbModPack.fileData = modpack.fileData
        return modpackRepository.save(dbModPack)
    }

    fun getModpack(id: Int): Optional<ModPack> {
        return modpackRepository.findById(id)
    }

    fun getModpacks(): List<ModPackView> {
        return modpackRepository.findAllProjectedBy()
    }

    fun getPackConfigForModpack(modpack: ModPack): PackConfig {
        val packConfig = PackConfig()
        packConfig.modpackDir = modpack.file
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
        if (modpack.fileData == null) {
            throw IllegalArgumentException("Modpack ${modpack.id} does not have data to export.")
        }
        var zipPath: Path = File(apiProperties.modpacksDirectory, modpack.file).toPath()
        // Does an archive with the same name already exist?
        if (zipPath.toFile().isFile) {
            var incrementation = 0
            val substring = zipPath.toString().substring(0, zipPath.toString().length - 4)
            while (File("${substring}_$incrementation.zip").isFile) {
                incrementation++
            }
            zipPath = Paths.get("${substring}_$incrementation.zip")
        }
        /*FileOutputStream(zipPath.toFile()).use {
            IOUtils.copy(modpack.data!!.binaryStream, it)
        }*/
        Files.write(zipPath, modpack.fileData!!.data!!)
        return zipPath.toFile()
    }
}