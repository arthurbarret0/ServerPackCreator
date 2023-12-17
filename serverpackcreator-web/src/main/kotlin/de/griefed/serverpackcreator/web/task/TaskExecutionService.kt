package de.griefed.serverpackcreator.web.task

import de.griefed.serverpackcreator.web.data.ModPack

interface TaskExecutionService {
    fun submitTaskInQueue(taskDetail: TaskDetail)

    fun getQueueSize(): Int

    fun clearQueue(): String

    fun removeTaskForModpack(modpack: ModPack): String

    fun getQueueDetails(): List<TaskDetail>
}