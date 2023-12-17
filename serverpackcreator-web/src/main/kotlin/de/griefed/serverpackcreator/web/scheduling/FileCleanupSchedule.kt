package de.griefed.serverpackcreator.web.scheduling

import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class FileCleanupSchedule {
    private val log = cachedLoggerOf(this.javaClass)

    @Scheduled(cron = "\${de.griefed.serverpackcreator.spring.schedules.files.cleanup}")
    private fun cleanFiles() {
        log.info("Cleaning files...")
        log.info("File cleanup completed.")
    }
}