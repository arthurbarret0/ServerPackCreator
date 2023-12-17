package de.griefed.serverpackcreator.web.data

import de.griefed.serverpackcreator.web.modpack.ModpackSource
import de.griefed.serverpackcreator.web.modpack.ModpackStatus
import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode

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

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.LAZY)
    var serverPack: MutableList<ServerPack> = mutableListOf()

    @Fetch(FetchMode.SELECT)
    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    var fileData: FileData? = null

    constructor()
    constructor(
        id: Int,
        projectID: String,
        versionID: String,
        minecraftVersion: String,
        modloader: String,
        modloaderVersion: String,
        clientMods: String,
        whiteListMods: String,
        name: String,
        file: String,
        size: Double,
        status: ModpackStatus,
        source: ModpackSource,
        serverPack: MutableList<ServerPack>,
        data: FileData?
    ) {
        this.id = id
        this.projectID = projectID
        this.versionID = versionID
        this.minecraftVersion = minecraftVersion
        this.modloader = modloader
        this.modloaderVersion = modloaderVersion
        this.clientMods = clientMods
        this.whiteListMods = whiteListMods
        this.name = name
        this.file = file
        this.size = size
        this.status = status
        this.source = source
        this.serverPack = serverPack
        this.fileData = data
    }

    constructor(
        projectID: String,
        versionID: String,
        minecraftVersion: String,
        modloader: String,
        modloaderVersion: String,
        clientMods: String,
        whiteListMods: String,
        name: String,
        file: String,
        size: Double,
        status: ModpackStatus,
        source: ModpackSource,
        serverPack: MutableList<ServerPack>,
        data: FileData?
    ) {
        this.projectID = projectID
        this.versionID = versionID
        this.minecraftVersion = minecraftVersion
        this.modloader = modloader
        this.modloaderVersion = modloaderVersion
        this.clientMods = clientMods
        this.whiteListMods = whiteListMods
        this.name = name
        this.file = file
        this.size = size
        this.status = status
        this.source = source
        this.serverPack = serverPack
        this.fileData = data
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
        if (size != other.size) return false
        if (source != other.source) return false
        if (fileData != other.fileData) return false

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
        result = 31 * result + size.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + (fileData?.hashCode() ?: 0)
        return result
    }
}