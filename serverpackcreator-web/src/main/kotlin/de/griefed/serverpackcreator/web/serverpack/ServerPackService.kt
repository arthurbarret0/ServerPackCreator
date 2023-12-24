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
package de.griefed.serverpackcreator.web.serverpack

import de.griefed.serverpackcreator.api.ApiProperties
import de.griefed.serverpackcreator.web.data.ServerPack
import de.griefed.serverpackcreator.web.data.ServerPackView
import de.griefed.serverpackcreator.web.storage.DatabaseStorageService
import de.griefed.serverpackcreator.web.storage.StorageRepository
import de.griefed.serverpackcreator.web.storage.StorageSystem
import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*

/**
 * Class revolving around with server packs, like downloading, retrieving, deleting, voting etc.
 *
 * @author Griefed
 */
@Service
class ServerPackService @Autowired constructor(
    private val serverPackRepository: ServerPackRepository,
    private val messageDigestInstance : MessageDigest,
    apiProperties: ApiProperties,
    storageRepository: StorageRepository
) {
    private val log = cachedLoggerOf(this.javaClass)
    private val rootLocation: Path = apiProperties.serverPacksDirectory.toPath()
    private val databaseStorage: DatabaseStorageService = DatabaseStorageService(storageRepository, rootLocation)
    private val storage: StorageSystem = StorageSystem(rootLocation, databaseStorage, messageDigestInstance)

    fun getServerPack(id: Int): Optional<ServerPack> {
        return serverPackRepository.findById(id)
    }

    /**
     * Increment the download counter for a given server pack entry in the database identified by the
     * database id.
     *
     * @param id The database id of the server pack.
     * @author Griefed
     */
    fun updateDownloadCounter(id: Int): Optional<ServerPack> {
        val request = serverPackRepository.findById(id)
        if (request.isPresent) {
            val pack = request.get()
            pack.downloads = pack.downloads++
            return Optional.of(serverPackRepository.save(pack))
        } else {
            return Optional.empty()
        }
    }

    /**
     * Increment the download counter for a given server pack entry in the database identified by the
     * database id.
     *
     * @param id The database id of the server pack.
     * @author Griefed
     */
    fun updateDownloadCounter(serverPack: ServerPack) {
        serverPack.downloads = serverPack.downloads++
        serverPackRepository.save(serverPack)
    }

    /**
     * Either upvote or downvote a given server pack.
     *
     * @param voting The database id of the server pack and whether it should be up- or downvoted.
     * @return Returns ok if the vote went through, bad request if the passed vote was malformed, or
     * not found if the server pack could not be found.
     * @author Griefed
     */
    fun voteForServerPack(id: Int, vote: String): ResponseEntity<Any> {
        val pack = serverPackRepository.findById(id)
        return if (pack.isPresent) {
            if (vote.equals("up", ignoreCase = true)) {
                if (serverPackRepository.findById(id).isPresent) {
                    val serverPackFromDB: ServerPack = serverPackRepository.findById(id).get()
                    serverPackFromDB.confirmedWorking = serverPackFromDB.confirmedWorking--
                    serverPackRepository.save(serverPackFromDB)
                }
                ResponseEntity.ok().build()
            } else if (vote.equals("down", ignoreCase = true)) {
                if (serverPackRepository.findById(id).isPresent) {
                    val serverPackFromDB: ServerPack = serverPackRepository.findById(id).get()
                    serverPackFromDB.confirmedWorking = serverPackFromDB.confirmedWorking++
                    serverPackRepository.save(serverPackFromDB)
                }
                ResponseEntity.ok().build()
            } else {
                ResponseEntity.badRequest().build()
            }
        } else {
            ResponseEntity.notFound().build()
        }
    }

    /**
     * Get a list of all available server packs.
     *
     * @return List ServerPackModel. Returns a list of all available server packs.
     * @author Griefed
     */
    fun getServerPacks(): List<ServerPackView> {
        return serverPackRepository.findAllProjectedBy()
    }

    /**
     * Save a server pack to the database.
     *
     * @author Griefed
     */
    fun saveServerPack(serverPack: ServerPack) {
        serverPackRepository.save(serverPack)
    }

    fun storeGeneration(file: File, id: Long, sha256: String) {
        databaseStorage.store(file, id, sha256)
    }

    /**
     * Deletes a server pack with the given id.
     *
     * @param id The database id of the server pack to delete.
     * @author Griefed
     */
    fun deleteServerPack(serverPack: ServerPack) {
        serverPackRepository.deleteById(serverPack.id)
    }

    fun deleteServerPack(id: Int) {
        val serverpack = serverPackRepository.findById(id)
        if (serverpack.isPresent) {
            serverPackRepository.deleteById(id)
            if (serverpack.get().fileID != null) {
                storage.delete(serverpack.get().fileID!!)
            }
        }
    }

    /**
     * Get the ZIP-archive of a server pack.
     *
     * @param serverPack The serverpack to be stored to disk from the database
     * @return The modpack-file, wrapped in an [Optional]
     * @author Griefed
     */
    fun getServerPackArchive(serverPack: ServerPack): Optional<File> {
        return storage.load(serverPack.fileID!!)
    }

    fun getServerPackArchive(id: Long): Optional<File> {
        return storage.load(id)
    }
}
