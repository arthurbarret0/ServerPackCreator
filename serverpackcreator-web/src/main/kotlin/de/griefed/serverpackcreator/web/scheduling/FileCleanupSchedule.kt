/* Copyright (C) 2023  Griefed
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 *
 * The full license can be found at https:github.com/Griefed/ServerPackCreator/blob/main/LICENSE
 */
package de.griefed.serverpackcreator.web.scheduling

import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class FileCleanupSchedule {
    private val log = cachedLoggerOf(this.javaClass)
    //TODO sync file to database
    //TODO delete files not linked in database
    @Scheduled(cron = "\${de.griefed.serverpackcreator.spring.schedules.files.cleanup}")
    private fun cleanFiles() {
        log.info("Cleaning files...")
        log.info("File cleanup completed.")
    }
}