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

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp

@Entity
class ServerPack() {

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

    @ManyToOne(fetch = FetchType.EAGER)
    var runConfiguration: RunConfiguration? = null

    constructor(
        size: Double,
        downloads: Int,
        confirmedWorking: Int,
        dateCreated: Timestamp?,
        runConfiguration: RunConfiguration?,
        fileID: Long?,
        fileHash: String?
    ) : this() {
        this.size = size
        this.downloads = downloads
        this.confirmedWorking = confirmedWorking
        this.dateCreated = dateCreated
        this.runConfiguration = runConfiguration
        this.fileID = fileID
        this.fileHash = fileHash
    }

}