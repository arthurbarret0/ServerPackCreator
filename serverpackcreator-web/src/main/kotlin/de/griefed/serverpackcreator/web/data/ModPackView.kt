package de.griefed.serverpackcreator.web.data

import de.griefed.serverpackcreator.web.modpack.ModpackSource
import de.griefed.serverpackcreator.web.modpack.ModpackStatus

interface ModPackView {
    var id: Int
    var projectID: String
    var versionID: String
    var minecraftVersion: String
    var modloader: String
    var modloaderVersion: String
    var clientMods: String
    var whiteListMods: String
    var name: String
    var size: Double
    var status: ModpackStatus
    var source: ModpackSource
    var serverPack: MutableList<ServerPack>
}