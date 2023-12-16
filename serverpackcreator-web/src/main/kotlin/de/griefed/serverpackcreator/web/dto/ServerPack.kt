/* Copyright (C) 2023  Griefed
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
 *
 * The full license can be found at https:github.com/Griefed/ServerPackCreator/blob/main/LICENSE
 */
package de.griefed.serverpackcreator.web.dto

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp

@Entity
class ServerPack {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    var id = 0

    @ManyToOne
    var modpack: ModPack? = null

    @Column
    var size: Double = 0.0

    @Column
    var downloads: Int = 0

    @Column
    var confirmedWorking: Int = 0

    @Lob
    @Column(length = Integer.MAX_VALUE)
    var data: ByteArray? = null

    @CreationTimestamp
    @Column
    var dateCreated: Timestamp? = null

    constructor()

    constructor(
        id: Int,
        modpack: ModPack?,
        size: Double,
        downloads: Int,
        confirmedWorking: Int,
        data: ByteArray?,
        dateCreated: Timestamp?
    ) {
        this.id = id
        this.modpack = modpack
        this.size = size
        this.downloads = downloads
        this.confirmedWorking = confirmedWorking
        this.data = data
        this.dateCreated = dateCreated
    }

    constructor(
        modpack: ModPack?,
        size: Double,
        downloads: Int,
        confirmedWorking: Int,
        data: ByteArray?,
        dateCreated: Timestamp?
    ) {
        this.modpack = modpack
        this.size = size
        this.downloads = downloads
        this.confirmedWorking = confirmedWorking
        this.data = data
        this.dateCreated = dateCreated
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerPack

        if (modpack != other.modpack) return false
        if (size != other.size) return false
        if (data != null) {
            if (other.data == null) return false
            if (!data.contentEquals(other.data)) return false
        } else if (other.data != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = modpack?.hashCode() ?: 0
        result = 31 * result + size.hashCode()
        result = 31 * result + (data?.contentHashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ServerPack(id=$id, modpack=$modpack, size=$size, downloads=$downloads, confirmedWorking=$confirmedWorking, data=${data?.contentToString()}, dateCreated=$dateCreated)"
    }
}