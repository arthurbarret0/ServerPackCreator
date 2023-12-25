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
package de.griefed.serverpackcreator.web.scheduling

import de.griefed.serverpackcreator.api.ApiProperties
import de.griefed.serverpackcreator.api.utilities.common.deleteQuietly
import de.griefed.serverpackcreator.web.modpack.ModpackRepository
import de.griefed.serverpackcreator.web.serverpack.ServerPackRepository
import de.griefed.serverpackcreator.web.storage.StorageRepository
import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.File
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries

@Service
class FileCleanupSchedule @Autowired constructor(
    private val modpackRepository: ModpackRepository,
    private val serverPackRepository: ServerPackRepository,
    private val fileStorageRepository: StorageRepository,
    apiProperties: ApiProperties
) {
    private val log = cachedLoggerOf(this.javaClass)
    private val modPackRoot: Path = apiProperties.modpacksDirectory.toPath()
    private val serverPackRoot: Path = apiProperties.serverPacksDirectory.toPath()

    @Scheduled(cron = "\${de.griefed.serverpackcreator.spring.schedules.files.cleanup}")
    private fun cleanFiles() {
        log.info("Cleaning files...")
        val fileIds: MutableList<Int> = mutableListOf()
        fileIds.addAll(modpackRepository.findAll().filter { it.fileID != null }.map { it.fileID!!.toInt() })
        fileIds.addAll(serverPackRepository.findAll().filter { it.fileID != null }.map { it.fileID!!.toInt() })
        fileIds.addAll(fileStorageRepository.findAll().map { it.id.toInt() })

        val files: MutableList<File> = mutableListOf()
        files.addAll(modPackRoot.listDirectoryEntries().map { it.toFile() })
        files.addAll(serverPackRoot.listDirectoryEntries().map { it.toFile() })

        for (file in files) {
            if (!fileIds.any { file.name.contains(it.toString()) }) {
                file.deleteQuietly()
                log.info("Deleted ${file.absolutePath} as it was not found in any repository.")
            }
        }
        log.info("File cleanup completed.")
    }
}