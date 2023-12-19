package de.griefed.serverpackcreator.web.storage

import de.griefed.serverpackcreator.web.data.StorageData
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

class DatabaseStorageService (private val storageRepository: StorageRepository, private val rootLocation: Path) {

    fun store(file: MultipartFile, id: Long, hash: BigInteger): Optional<Pair<Long, BigInteger>> {
        val data = StorageData(id!!, hash!!, file.bytes)
        storageRepository.save(data)
        return Optional.of(Pair(id, hash))
    }

    fun load(id: Long): Optional<File> {
        val data = storageRepository.findById(id)
        if (data.isEmpty) {
            return Optional.empty()
        }
        val file = File(rootLocation.toFile(),"${id}.zip")
        Files.write(file.toPath(),data.get().data)
        return Optional.of(file)
    }

    fun delete(id: Long) {
        TODO("Not yet implemented")
    }

    fun deleteAll() {
        TODO("Not yet implemented")
    }
}