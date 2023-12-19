package de.griefed.serverpackcreator.web.storage

import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.math.BigInteger
import java.util.*

interface StorageService {

    fun store(file: MultipartFile): Optional<Pair<Long, BigInteger>>

    fun load(id: Long): Optional<File>

    fun delete(id: Long)

    fun deleteAll()
}