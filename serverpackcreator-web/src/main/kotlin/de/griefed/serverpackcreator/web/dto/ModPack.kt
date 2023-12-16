package de.griefed.serverpackcreator.web.dto

import de.griefed.serverpackcreator.web.modpack.ModpackSource
import de.griefed.serverpackcreator.web.modpack.ModpackStatus
import jakarta.persistence.*

@Entity
class ModPack {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    var id = 0

    @Column
    var projectID: String = ""

    @Column
    var versionID: String = ""

    @Column
    var minecraftVersion: String = ""

    @Column
    var modloader: String = ""

    @Column
    var modloaderVersion: String = ""

    @Lob
    @Column
    var clientMods: String = ""

    @Lob
    @Column
    var whiteListMods: String = ""

    @Column
    var name: String = ""

    @Column
    var file: String = ""

    @Column
    var size: Double = 0.0

    @Column
    var status: ModpackStatus = ModpackStatus.QUEUED

    @Column
    var source: ModpackSource = ModpackSource.ZIP

    @OneToMany
    var serverPack: List<ServerPack> = listOf()

    @Lob
    @Column(length = Integer.MAX_VALUE)
    var data: ByteArray? = null

    constructor()

    constructor(
        id: Int,
        projectID: String,
        versionID: String,
        minecraftVersion: String,
        modloader: String,
        modloaderVersion: String,
        clientMods: String,
        whiteList: String,
        name: String,
        fileName: String,
        size: Double,
        status: ModpackStatus,
        source: ModpackSource,
        data: ByteArray?
    ) {
        this.id = id
        this.projectID = projectID
        this.versionID = versionID
        this.minecraftVersion = minecraftVersion
        this.modloader = modloader
        this.modloaderVersion = modloaderVersion
        this.clientMods = clientMods
        this.whiteListMods = whiteList
        this.name = name
        this.file = fileName
        this.size = size
        this.status = status
        this.source = source
        this.data = data
    }

    constructor(
        projectID: String,
        versionID: String,
        minecraftVersion: String,
        modloader: String,
        modloaderVersion: String,
        clientMods: String,
        whiteList: String,
        name: String,
        fileName: String,
        size: Double,
        status: ModpackStatus,
        source: ModpackSource,
        data: ByteArray?
    ) {
        this.projectID = projectID
        this.versionID = versionID
        this.minecraftVersion = minecraftVersion
        this.modloader = modloader
        this.modloaderVersion = modloaderVersion
        this.clientMods = clientMods
        this.whiteListMods = whiteList
        this.name = name
        this.file = fileName
        this.size = size
        this.status = status
        this.source = source
        this.data = data
    }

    override fun toString(): String {
        return "Modpack(id=$id, projectID='$projectID', versionID='$versionID', name='$name', fileName='$file', size=$size, status='$status', source=$source, data=${data?.contentToString()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModPack

        if (projectID != other.projectID) return false
        if (versionID != other.versionID) return false
        if (minecraftVersion != other.minecraftVersion) return false
        if (modloader != other.modloader) return false
        if (modloaderVersion != other.modloaderVersion) return false
        if (clientMods != other.clientMods) return false
        if (whiteListMods != other.whiteListMods) return false
        if (name != other.name) return false
        if (file != other.file) return false
        if (size != other.size) return false
        if (source != other.source) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = projectID.hashCode()
        result = 31 * result + versionID.hashCode()
        result = 31 * result + minecraftVersion.hashCode()
        result = 31 * result + modloader.hashCode()
        result = 31 * result + modloaderVersion.hashCode()
        result = 31 * result + clientMods.hashCode()
        result = 31 * result + whiteListMods.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + file.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }
}