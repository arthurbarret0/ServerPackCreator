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

@Entity
class RunConfiguration() {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    var id: Int = 0

    @Column
    var minecraftVersion: String = ""

    @Column
    var modloader: String = ""

    @Column
    var modloaderVersion: String = ""

    @Column
    var startArgs: String = ""

    @ManyToMany(fetch = FetchType.EAGER)
    var clientMods: MutableList<ClientMod> = mutableListOf()

    @ManyToMany(fetch = FetchType.EAGER)
    var whitelistedMods: MutableList<WhitelistedMod> = mutableListOf()

    constructor(
        minecraftVersion: String,
        modloader: String,
        modloaderVersion: String,
        startArgs: String,
        clientMods: MutableList<ClientMod>,
        whitelistedMods: MutableList<WhitelistedMod>
    ) : this() {
        this.minecraftVersion = minecraftVersion
        this.modloader = modloader
        this.modloaderVersion = modloaderVersion
        this.startArgs = startArgs
        this.clientMods = clientMods
        this.whitelistedMods = whitelistedMods
    }

    //TODO equals, hash, toString
}