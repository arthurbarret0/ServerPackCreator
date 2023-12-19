package de.griefed.serverpackcreator.web.task

import de.griefed.serverpackcreator.api.ConfigurationHandler
import de.griefed.serverpackcreator.api.ServerPackHandler
import de.griefed.serverpackcreator.api.utilities.common.deleteQuietly
import de.griefed.serverpackcreator.api.utilities.common.size
import de.griefed.serverpackcreator.web.data.ModPackFile
import de.griefed.serverpackcreator.web.data.ModPack
import de.griefed.serverpackcreator.web.data.ServerPack
import de.griefed.serverpackcreator.web.modpack.ModpackService
import de.griefed.serverpackcreator.web.modpack.ModpackSource
import de.griefed.serverpackcreator.web.modpack.ModpackStatus
import de.griefed.serverpackcreator.web.serverpack.ServerPackService
import org.apache.logging.log4j.kotlin.KotlinLogger
import org.apache.logging.log4j.kotlin.cachedLoggerOf
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque


@Service
class TaskExecutionServiceImpl @Autowired constructor(
    private val modpackService: ModpackService,
    private val serverPackService: ServerPackService,
    private val configurationHandler: ConfigurationHandler,
    private val serverPackHandler: ServerPackHandler
) :
    TaskExecutionService {
    private val logger: KotlinLogger = cachedLoggerOf(this.javaClass)
    private val blockingQueue: BlockingQueue<TaskDetail> = LinkedBlockingDeque()

    init {
        initiateThread()
    }

    /**
     * Single Thread on Which Tasks will be performed
     */
    private fun initiateThread() {
        val thread = Thread {
            while (true) {
                try {
                    if (!blockingQueue.isEmpty()) {
                        logger.info("Processing Next Task from Queue")
                        val taskDetail = blockingQueue.take()
                        processTask(taskDetail)
                    }
                } catch (e: InterruptedException) {
                    logger.error("There was an error while processing ", e)
                    Thread.currentThread().interrupt()
                }
            }
        }
        thread.name = "GenerationThread"
        thread.start()
        logger.info("Worker Thread ${thread.name} initiated successfully")
    }

    /**
     * Method to Submit Tasks in Queue
     */
    override fun submitTaskInQueue(taskDetail: TaskDetail) {
        blockingQueue.add(taskDetail)
        logger.info("Task for Server Pack : ${taskDetail.modpack.id} submitted in Queue")
    }

    private fun processTask(taskDetail: TaskDetail) {
        logger.info("Running on Thread ${Thread.currentThread().name}")
        when (taskDetail.modpack.status) {
            ModpackStatus.QUEUED -> checkModpack(taskDetail.modpack)
            ModpackStatus.CHECKED -> {
                if (taskDetail.modpack.source == ModpackSource.ZIP) {
                    generateFromZip(taskDetail.modpack)
                } else {
                    generateFromModrinth(taskDetail.modpack)
                }
            }
            else -> logger.error("${taskDetail.modpack.status} does not merit unique processing.")
        }
    }

    private fun checkModpack(modpack: ModPack) {
        logger.info("Performing Modpack check for modpack : ${modpack.id}")
        val exportedZip = modpackService.exportModpackToDisk(modpack)
        modpack.status = ModpackStatus.CHECKING
        modpackService.saveModpack(modpack)
        modpack.file = exportedZip.absolutePath
        val packConfig = modpackService.getPackConfigForModpack(modpack)
        val check = configurationHandler.checkConfiguration(packConfig)
        if (modpack.file != packConfig.modpackDir) {
            File(modpack.file).deleteQuietly()
        }
        modpack.file = packConfig.modpackDir
        if (check.allChecksPassed) {
            modpack.status = ModpackStatus.CHECKED
            submitTaskInQueue(TaskDetail(modpack))
        } else {
            modpack.status = ModpackStatus.ERROR
            exportedZip.deleteQuietly()
            File(packConfig.modpackDir).deleteQuietly()
            modpack.file = ""
        }
        modpackService.saveModpack(modpack)
    }

    private fun generateFromModrinth(modpack: ModPack) {
        logger.info("Server Pack will be generated from Modrinth modpack : ${modpack.id}")
        logger.warn("Modrinth API will be available in Milestone 6.")
        /*logger.info("Server Pack generated.")*/
    }

    private fun generateFromZip(modpack: ModPack) {
        logger.info("Server Pack will be generated from uploaded, zipped, modpack : ${modpack.id}")
        modpack.status = ModpackStatus.GENERATING
        modpackService.saveModpack(modpack)
        val packConfig = modpackService.getPackConfigForModpack(modpack)
        if (serverPackHandler.run(packConfig)) {
            val destination = serverPackHandler.getServerPackDestination(packConfig)
            val serverPackZip = File("${destination}_server_pack.zip")
            val serverPack = ServerPack()
            serverPack.modpack = modpack
            serverPack.size = serverPackZip.size().div(1048576.0)
            serverPack.modPackFile = ModPackFile()
            serverPack.modPackFile!!.data = serverPackZip.readBytes()
            modpack.serverPack.addLast(serverPack)
            modpack.status = ModpackStatus.GENERATED
            logger.info("Storing server pack : ${serverPack.id}")
            serverPackService.saveServerPack(serverPack)
            File(destination).deleteQuietly()
        } else {
            modpack.status = ModpackStatus.ERROR
        }
        File(modpack.file).deleteQuietly()
        modpack.file = ""
        modpackService.saveModpack(modpack)
        logger.info("Server Pack generated.")
    }

    /**
     * Method to get Queue Size
     */
    override fun getQueueSize(): Int {
        return blockingQueue.size
    }

    /**
     * Method to clear all tasks from queue
     */
    override fun clearQueue(): String {
        val size = getQueueSize()
        blockingQueue.clear()
        return "Cleared Queue. It had total tasks : $size"
    }

    /**
     * Method to remove a Task from Queue
     */
    override fun removeTaskForModpack(modpack: ModPack): String {
        val taskList = blockingQueue.stream().filter { task: TaskDetail ->
            task.modpack == modpack
        }.toList().toSet()
        blockingQueue.removeAll(taskList)
        return "Total ${taskList.size} removed from Queue."
    }

    /**
     * Method to get all Task details present in Queue
     */
    override fun getQueueDetails(): List<TaskDetail> {
        return blockingQueue.stream().toList()
    }
}