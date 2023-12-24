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
package de.griefed.serverpackcreator.web.storage

import de.griefed.serverpackcreator.api.utilities.common.size
import de.griefed.serverpackcreator.web.data.StorageData
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.hibernate.engine.jdbc.BlobProxy
import org.springframework.util.FileCopyUtils
import java.io.File
import java.nio.file.Path
import java.util.*

class DatabaseStorageService(private val storageRepository: StorageRepository, private val rootLocation: Path) {
    private val logger: KotlinLogger = cachedLoggerOf(this.javaClass)

    @OptIn(DelicateCoroutinesApi::class)
    fun store(file: File, id: Long, hash: String) {
        GlobalScope.launch {
            logger.debug("    ID: $id")
            logger.debug("SHA256: $hash")
            val data = StorageData(id, hash, BlobProxy.generateProxy(file.inputStream(), file.size().toLong()))
            storageRepository.save(data)
        }
    }

    fun load(id: Long): Optional<File> {
        val data = storageRepository.findById(id)
        if (data.isEmpty) {
            logger.warn("Database does not contain an entry for $id.")
            return Optional.empty()
        } else {
            val file = rootLocation.resolve("${id}.zip").normalize().toFile()
            //Files.write(file.toPath(), data.get().data)
            FileCopyUtils.copy(data.get().data.binaryStream, file.outputStream())
            return Optional.of(file)
        }
    }

    fun delete(id: Long) {
        storageRepository.deleteById(id)
    }

    fun deleteAll() {
        storageRepository.deleteAll()
    }
}