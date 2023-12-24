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

import de.griefed.serverpackcreator.web.modpack.ModpackSource
import de.griefed.serverpackcreator.web.modpack.ModpackStatus
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.sql.Timestamp

@Entity
class ModPack {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    var id : Int = 0

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

    @ManyToMany(fetch = FetchType.EAGER)
    var clientMods: MutableList<ClientMod> = mutableListOf()

    @ManyToMany(fetch = FetchType.EAGER)
    var whitelistedMods: MutableList<WhitelistedMod> = mutableListOf()

    @CreationTimestamp
    @Column
    var dateCreated: Timestamp? = null

    @Column
    var name: String = ""

    @Column
    var size: Double = 0.0

    @Column
    var status: ModpackStatus = ModpackStatus.QUEUED

    @Column
    var source: ModpackSource = ModpackSource.ZIP

    @Column
    var fileID: Long? = null

    @Column
    var fileHash: String? = null

    @OneToMany
    var serverPacks: MutableList<ServerPack> = mutableListOf()
}