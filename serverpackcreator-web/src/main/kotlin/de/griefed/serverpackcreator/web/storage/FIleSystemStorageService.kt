package de.griefed.serverpackcreator.web.storage

import org.springframework.util.FileSystemUtils
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.IOException
import java.math.BigInteger
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.*
import kotlin.io.path.listDirectoryEntries

class FileSystemStorageService(private val rootLocation: Path) {
    private val messageDigestInstance = MessageDigest.getInstance("SHA-256")

    fun store(file: MultipartFile, id: Long): Optional<Pair<Long, BigInteger>> {
        try {
            if (file.isEmpty) {
                throw StorageException("Failed to store empty file.")
            }
            val name = System.currentTimeMillis().toString()
            val destinationFile: Path = rootLocation.resolve(name).normalize().toAbsolutePath()
            if (!destinationFile.parent.equals(rootLocation.toAbsolutePath())) {
                // This is a security check
                throw StorageException("Cannot store file outside current directory.")
            }
            file.inputStream.use { inputStream ->
                Files.copy(
                    inputStream, destinationFile,
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
            val hash = BigInteger(1, messageDigestInstance.digest(file.bytes)).toString(16)
            return Optional.of(Pair(name, hash))
        } catch (e: IOException) {
            return Optional.empty()
        }
    }

    fun load(id: Long): Optional<File> {
        val file = rootLocation.resolve(id).toFile()
        return if (file.exists()) {
            Optional.of(file)
        } else {
            Optional.empty()
        }

    }

    fun delete(id: Long) {
        FileSystemUtils.deleteRecursively(rootLocation.resolve(id).toFile())
    }

    fun deleteAll() {
        for (path in rootLocation.listDirectoryEntries()) {
            FileSystemUtils.deleteRecursively(path)
        }
    }
}