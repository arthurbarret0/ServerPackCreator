/* Copyright (C) 2022  Griefed
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
package de.griefed.serverpackcreator.spring.models;

/**
 * <a href="https://dev.to/gotson/how-to-implement-a-task-queue-using-apache-artemis-and-spring-boot-2mme">How to implement a task queue using Apache Artemis and Spring Boot</a><br>
 * Huge Thank You to <a href="https://github.com/gotson">Gauthier</a> for writing the above guide on how to implement a JMS. Without it this implementation of Artemis
 * would have either taken way longer or never happened at all. I managed to translate their Kotlin-code to Java and make
 * the necessary changes to fully implement it in ServerPackCreator.<br>
 * Tasks related to generating a server pack from CurseForge.
 * @author Griefed
 */
public class GenerateCurseProject extends Task {

    private final String projectIDAndFileID;

    /**
     * Send a message with a task for a CurseForge generation.
     * @author Griefed
     * @param projectIDAndFileID String. The project and file id combination of the submitted task.
     */
    public GenerateCurseProject(String projectIDAndFileID) {
        this.projectIDAndFileID = projectIDAndFileID;
    }

    /**
     * Getter for the project and file id combination of the submitted task.
     * @author Griefed
     * @return Returns the project and file id combination of the submitted task.
     */
    public String getProjectIDAndFileID() {
        return this.projectIDAndFileID;
    }

    /**
     * Getter for the unique id of the submitted task.
     * @author Griefed
     * @return String. Returns the unique id of the submitted task.
     */
    @Override
    public String uniqueId() {
        return "GENERATE_CURSEPROJECT_" + projectIDAndFileID;
    }
}