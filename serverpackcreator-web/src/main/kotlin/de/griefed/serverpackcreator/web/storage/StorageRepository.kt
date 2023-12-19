package de.griefed.serverpackcreator.web.storage

import de.griefed.serverpackcreator.web.data.StorageData
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StorageRepository : JpaRepository<StorageData, Long> {
}