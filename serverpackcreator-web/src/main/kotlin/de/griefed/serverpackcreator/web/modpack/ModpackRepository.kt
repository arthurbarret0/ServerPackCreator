package de.griefed.serverpackcreator.web.modpack

import de.griefed.serverpackcreator.web.dto.ModPack
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ModpackRepository : JpaRepository<ModPack, Int>