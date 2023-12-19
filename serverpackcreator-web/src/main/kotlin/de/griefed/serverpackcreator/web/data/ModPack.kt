package de.griefed.serverpackcreator.web.data

import de.griefed.serverpackcreator.web.modpack.ModpackSource
import de.griefed.serverpackcreator.web.modpack.ModpackStatus
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigInteger
import java.sql.Timestamp

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
    var status: ModpackStatus = ModpackStatus.QUEUED

    @CreationTimestamp
    @Column
    var dateCreated: Timestamp? = null

    @Column
    var name: String = ""

    @Column
    var size: Double = 0.0

    @Column
    var source: ModpackSource = ModpackSource.ZIP

    @Column
    var fileID: Int? = null

    @Column
    var fileHash: BigInteger? = null

    @OneToMany
    var serverPack: MutableList<ServerPack> = mutableListOf()

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
        if (size != other.size) return false
        if (source != other.source) return false
        if (fileID != other.fileID) return false
        if (fileHash != other.fileHash) return false

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
        result = 31 * result + size.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + (fileID?.hashCode() ?: 0)
        result = 31 * result + (fileHash?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ModPack(id=$id, projectID='$projectID', versionID='$versionID', minecraftVersion='$minecraftVersion', modloader='$modloader', modloaderVersion='$modloaderVersion', clientMods='$clientMods', whiteListMods='$whiteListMods', name='$name', size=$size, status=$status, source=$source, fileID=$fileID, fileHash=$fileHash, serverPack=$serverPack)"
    }


}