package de.griefed.serverpackcreator.web.modpack

import de.griefed.serverpackcreator.web.NotificationService
import de.griefed.serverpackcreator.web.dto.ModPack
import de.griefed.serverpackcreator.web.task.TaskDetail
import de.griefed.serverpackcreator.web.task.TaskExecutionServiceImpl
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
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
    private val notificationService: NotificationService,
    private val taskExecutionServiceImpl: TaskExecutionServiceImpl
) {

    @GetMapping("/download/{id:[0-9]+}", produces = ["application/zip"])
    @ResponseBody
    fun downloadModpack(@PathVariable id: Int): ResponseEntity<Resource> {
        val modpack = modpackService.getModpack(id)
        return if (modpack.isPresent && modpack.get().data != null) {
            ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"${modpack.get().name}.zip\"")
                .body(ByteArrayResource(modpack.get().data!!))
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @PostMapping("/upload", produces = ["application/json"])
    @ResponseBody
    fun uploadModPack(@RequestParam("file") file: MultipartFile,
                      @RequestParam("minecraftVersion") minecraftVersion: String,
                      @RequestParam("modloader") modloader: String,
                      @RequestParam("modloaderVersion") modloaderVersion: String,
                      @RequestParam("clientMods") clientMods: String,
                      @RequestParam("whiteListMods") whiteListMods: String): ResponseEntity<String> {
        if (file.size == 0L ||
            file.bytes.isEmpty() ||
            !file.originalFilename!!.endsWith("zip", ignoreCase = true) ||
            minecraftVersion.isEmpty() ||
            modloader.isEmpty() ||
            modloaderVersion.isEmpty()) {
            return ResponseEntity.badRequest().header(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .body(notificationService.zipResponse(
                    messages = listOf("Invalid size or not a ZIP-file."),
                    timeout = 10000,
                    icon = "error",
                    colour = "negative",
                    file = file.name,
                    success = false
                ))
        }
        GlobalScope.launch {
            val modpack = modpackService.saveZipModpack(file, minecraftVersion, modloader, modloaderVersion, clientMods, whiteListMods)
            taskExecutionServiceImpl.submitTaskInQueue(TaskDetail(modpack))
        }
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
            .body(notificationService.zipResponse(
                messages = listOf("File is being stored and will be queued for checks."),
                timeout = 5000,
                icon = "info",
                colour = "positive",
                file.name,
                true
            ))
    }

    @GetMapping("/generate/{id:[0-9]+}", produces = ["application/json"])
    @ResponseBody
    fun requestGeneration(@PathVariable id: Int): ResponseEntity<String> {
        val modpack = modpackService.getModpack(id)
        if (modpack.isPresent) {
            taskExecutionServiceImpl.submitTaskInQueue(TaskDetail(modpack.get()))
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .body(notificationService.zipResponse(
                    messages = listOf("Modpack queued."),
                    timeout = 5000,
                    icon = "info",
                    colour = "positive",
                    file = "",
                    success = false
                ))
        } else {
            return ResponseEntity.badRequest().header(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON_VALUE)
                .body(notificationService.zipResponse(
                    messages = listOf("Modpack not found."),
                    timeout = 10000,
                    icon = "error",
                    colour = "negative",
                    file = "Not found",
                    success = false
                ))
        }
    }

    @GetMapping("/all", produces = ["application/json"])
    @ResponseBody
    fun getAllModPacks(): ResponseEntity<List<ModPack>> {
        return ResponseEntity.ok().header("Content-Type", "application/json").body(
            modpackService.getModpacks()
        )
    }
}