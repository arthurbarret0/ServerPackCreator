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
package de.griefed.serverpackcreator.web.data

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp

@Entity
class ServerPack {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    var id: Int = 0

    @Column
    var size: Double = 0.0

    @Column
    var downloads: Int = 0

    @Column
    var confirmedWorking: Int = 0

    @CreationTimestamp
    @Column
    var dateCreated: Timestamp? = null

    @Column
    var fileID: Long? = null

    @Column
    var fileHash: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ServerPack

        if (size != other.size) return false
        if (fileID != other.fileID) return false
        if (fileHash != other.fileHash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size.hashCode()
        result = 31 * result + (fileID?.hashCode() ?: 0)
        result = 31 * result + (fileHash?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ServerPack(id=$id, size=$size, downloads=$downloads, confirmedWorking=$confirmedWorking, dateCreated=$dateCreated, fileID=$fileID, fileHash=$fileHash)"
    }
}