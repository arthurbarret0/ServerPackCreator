package de.griefed.serverpackcreator.web.data

import java.sql.Timestamp

interface ServerPackView {
    var id: Int
    var modpack: ModPack?
    var size: Double
    var downloads: Int
    var confirmedWorking: Int
    var dateCreated: Timestamp
}