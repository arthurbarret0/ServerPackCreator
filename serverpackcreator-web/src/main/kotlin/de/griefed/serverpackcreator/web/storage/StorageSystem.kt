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

import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Path
import java.security.MessageDigest
import java.util.*

class StorageSystem(
    private val databaseStorage: DatabaseStorageService,
    private val fileSystemStorageService: FileSystemStorageService
) : StorageService {
    private val logger: KotlinLogger = cachedLoggerOf(this.javaClass)

    constructor(rootLocation: Path, databaseStorage: DatabaseStorageService, messageDigest: MessageDigest) : this(
        databaseStorage,
        FileSystemStorageService(rootLocation, messageDigest)
    )

    constructor(rootLocation: Path, storageRepository: StorageRepository) : this(
        DatabaseStorageService(storageRepository, rootLocation),
        FileSystemStorageService(rootLocation)
    )

    override fun store(file: MultipartFile): Optional<Pair<Long, String>> {
        val triple = fileSystemStorageService.store(file)
        databaseStorage.store(triple.get().third.toFile(), triple.get().first, triple.get().second)
        return Optional.of(Pair(triple.get().first, triple.get().second))
    }

    override fun load(id: Long): Optional<File> {
        val fileSys = fileSystemStorageService.load(id)
        if (fileSys.isPresent) {
            return fileSys
        }
        val dataSys = databaseStorage.load(id)
        if (dataSys.isPresent) {
            return dataSys
        }
        logger.error("File with ID $id could not be found.")
        return Optional.empty()
    }

    override fun delete(id: Long) {
        fileSystemStorageService.delete(id)
        databaseStorage.delete(id)
    }

    override fun deleteAll() {
        fileSystemStorageService.deleteAll()
        databaseStorage.deleteAll()
    }
}