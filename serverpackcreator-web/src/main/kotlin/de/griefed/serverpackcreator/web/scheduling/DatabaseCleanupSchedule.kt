package de.griefed.serverpackcreator.web.scheduling

import de.griefed.serverpackcreator.web.modpack.ModpackService
import de.griefed.serverpackcreator.web.modpack.ModpackStatus
import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class DatabaseCleanupSchedule @Autowired constructor(private val modpackService: ModpackService) {
    private val log = cachedLoggerOf(this.javaClass)

    @Scheduled(cron = "\${de.griefed.serverpackcreator.spring.schedules.database.cleanup}")
    private fun cleanDatabase() {
        log.info("Cleaning database...")
        for (view in modpackService.getModpacks()) {
            if (view.status == ModpackStatus.ERROR) {
                modpackService.deleteModpack(view.id)
                log.info("Deleted Modpack: ${view.id}-${view.name}")
            }
        }
        log.info("Database cleanup completed.")
    }
}