package de.griefed.serverpackcreator.web.customizing

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.NoRepositoryBean
import java.util.*

@NoRepositoryBean
interface ModRepository<T, ID> : JpaRepository<T, ID> {
    fun findByMod(mod: String) : Optional<T>
}