package de.griefed.serverpackcreator.web.task

import de.griefed.serverpackcreator.web.dto.ModPack
import de.griefed.serverpackcreator.web.dto.ServerPack

class TaskDetail(val modpack: ModPack) {

    var serverPack: ServerPack? = null
}