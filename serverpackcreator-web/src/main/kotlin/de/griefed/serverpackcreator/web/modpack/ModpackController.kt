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
package de.griefed.serverpackcreator.web.modpack

import de.griefed.serverpackcreator.web.NotificationService
import de.griefed.serverpackcreator.web.customizing.RunConfigurationService
import de.griefed.serverpackcreator.web.data.ModPackView
import de.griefed.serverpackcreator.web.task.TaskDetail
import de.griefed.serverpackcreator.web.task.TaskExecutionServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ByteArrayResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.util.MimeTypeUtils
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/api/v2/modpacks")
class ModpackController @Autowired constructor(
    private val modpackService: ModpackService,
    private val runConfigurationService: RunConfigurationService,
    private val notificationService: NotificationService,
    private val taskExecutionServiceImpl: TaskExecutionServiceImpl
) {

    @GetMapping("/download/{id:[0-9]+}", produces = ["application/zip"])
    @ResponseBody
    fun downloadModpack(@PathVariable id: Int): ResponseEntity<Resource> {
        val modpack = modpackService.getModPackArchive(id.toLong())
        return if (modpack.isPresent) {
            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${modpack.get().name}.zip\"")
                .body(ByteArrayResource(modpack.get().readBytes()))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping("/upload", produces = ["application/json"])
    @ResponseBody
    fun uploadModPack(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("minecraftVersion") minecraftVersion: String,
        @RequestParam("modloader") modloader: String,
        @RequestParam("modloaderVersion") modloaderVersion: String,
        @RequestParam("startArgs") startArgs: String,
        @RequestParam("clientMods") clientMods: String,
        @RequestParam("whiteListMods") whiteListMods: String
    ): ResponseEntity<String> {
        if (file.size == 0L ||
            file.bytes.isEmpty() ||
            !file.originalFilename!!.endsWith("zip", ignoreCase = true) ||
            minecraftVersion.isEmpty() ||
            modloader.isEmpty() ||
            modloaderVersion.isEmpty()
        ) {
            return ResponseEntity.badRequest().header(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .body(
                    notificationService.zipResponse(
                        messages = listOf("Invalid size or not a ZIP-file."),
                        timeout = 10000,
                        icon = "error",
                        colour = "negative",
                        file = file.name,
                        success = false
                    )
                )
        }
        val modpack = modpackService.saveZipModpack(file)
        val taskDetail = TaskDetail(modpack)
        taskDetail.runConfiguration = runConfigurationService.createRunConfig(
            minecraftVersion, modloader, modloaderVersion, startArgs, clientMods, whiteListMods
        )
        taskExecutionServiceImpl.submitTaskInQueue(taskDetail)
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
            .body(
                notificationService.zipResponse(
                    messages = listOf("File is being stored and will be queued for checks."),
                    timeout = 5000,
                    icon = "info",
                    colour = "positive",
                    modpack.id.toString(),
                    true
                )
            )
    }

    @GetMapping("/generate/{id:[0-9]+}", produces = ["application/json"])
    @ResponseBody
    fun requestGeneration(@PathVariable id: Int): ResponseEntity<String> {
        //TODO submit modpackID mcVersion, modloader, modloaderVersion, clientMods, whitelistMods
        //TODO configuration POJO linked as list to modpack, linked as one-to-one to server pack
        //TODO always generate server pack from configuration
        val modpack = modpackService.getModpack(id)
        if (modpack.isPresent) {
            taskExecutionServiceImpl.submitTaskInQueue(TaskDetail(modpack.get()))
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .body(
                    notificationService.zipResponse(
                        messages = listOf("Modpack queued."),
                        timeout = 5000,
                        icon = "info",
                        colour = "positive",
                        file = "",
                        success = false
                    )
                )
        } else {
            return ResponseEntity.badRequest().header(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .body(
                    notificationService.zipResponse(
                        messages = listOf("Modpack not found."),
                        timeout = 10000,
                        icon = "error",
                        colour = "negative",
                        file = "Not found",
                        success = false
                    )
                )
        }
    }

    @GetMapping("/all", produces = ["application/json"])
    @ResponseBody
    fun getAllModPacks(): ResponseEntity<List<ModPackView>> {
        return ResponseEntity.ok().header("Content-Type", "application/json").body(
            modpackService.getModpacks()
        )
    }
}