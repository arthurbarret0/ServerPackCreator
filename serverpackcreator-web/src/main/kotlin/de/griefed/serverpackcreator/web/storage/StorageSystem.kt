package de.griefed.serverpackcreator.web.storage

import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.math.BigInteger
import java.nio.file.Path
import java.util.*

class StorageSystem(private val rootLocation: Path, private val storageRepository: StorageRepository) :
    StorageService {

    private val fileSystemStorageService: FileSystemStorageService = FileSystemStorageService(rootLocation)
    private val databaseStorage: DatabaseStorageService = DatabaseStorageService(storageRepository, rootLocation)

    override fun store(file: MultipartFile): Optional<Pair<Long, BigInteger>> {
        TODO("Not yet implemented")
    }

    override fun load(id: Long): Optional<File> {
        TODO("Not yet implemented")
    }

    override fun delete(id: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() {
        TODO("Not yet implemented")
    }
}