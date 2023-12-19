package de.griefed.serverpackcreator.web.data

import java.math.BigInteger
import java.sql.Timestamp

interface ServerPackView {
    var id: Int
    var size: Double
    var downloads: Int
    var confirmedWorking: Int
    var dateCreated: Timestamp
    var fileHash: BigInteger
    var modpack: ModPack?
}