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
import jakarta.persistence.Id
import jakarta.persistence.Lob
import org.hibernate.annotations.JdbcTypeCode
import java.sql.Blob
import java.sql.Types

@Entity
class StorageData {

    @Id
    @Column(updatable = false, nullable = false)
    var id: Long

    @Column
    var hash: String

    @Lob
    @JdbcTypeCode(Types.BLOB)
    @Column(length = Integer.MAX_VALUE)
    var data: Blob

    constructor(id: Long, hash: String, data: Blob) {
        this.id = id
        this.hash = hash
        this.data = data
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StorageData

        if (hash != other.hash) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }

    override fun toString(): String {
        return "StorageData(id=$id, hash=$hash, data=$data)"
    }
}