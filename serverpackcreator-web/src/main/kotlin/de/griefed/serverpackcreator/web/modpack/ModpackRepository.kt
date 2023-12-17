package de.griefed.serverpackcreator.web.modpack

import de.griefed.serverpackcreator.web.data.ModPack
import de.griefed.serverpackcreator.web.data.ModPackView
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ModpackRepository : JpaRepository<ModPack, Int> {
    fun findAllProjectedBy(): MutableList<ModPackView>
}