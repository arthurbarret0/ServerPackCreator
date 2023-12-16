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

import de.griefed.serverpackcreator.web.dto.ServerPack
import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.util.*

/**
 * Class revolving around with server packs, like downloading, retrieving, deleting, voting etc.
 *
 * @author Griefed
 */
@Service
class ServerPackService @Autowired constructor(private val serverPackRepository: ServerPackRepository) {
    private val log = cachedLoggerOf(this.javaClass)

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
    fun getServerPacks(): List<ServerPack> {
        return serverPackRepository.findAll()
    }

    /**
     * Save a server pack to the database.
     *
     * @author Griefed
     */
    fun saveServerPack(
        serverPack: ServerPack
    ) {
        val serverPackFromDB = serverPackRepository.findById(serverPack.id)
        val pack: ServerPack = if (serverPackFromDB.isPresent) {
            serverPackFromDB.get()
        } else {
            ServerPack()
        }
        pack.modpack = serverPack.modpack
        pack.size = serverPack.size
        pack.downloads = serverPack.downloads
        pack.confirmedWorking = serverPack.confirmedWorking
        pack.data = serverPack.data
        pack.dateCreated = serverPack.dateCreated
        serverPackRepository.save(pack)
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
}
