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

import de.griefed.serverpackcreator.ConfigurationModel;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Class containing all fields and therefore all information gathered from a submitted CurseForge project and fileID, or
 * modpack export. By extending {@link ConfigurationModel} we inherit all basic fields required for the generation of a
 * server pack and can add only those we require in the REST API portion of ServerPackCreator.<br>
 * We mark this class with {@link Entity} because we also use this class for storing information in our database.
 * @author Griefed
 */
@Entity
public class ServerPack extends ConfigurationModel {

    /**
     * Constructor for our ServerPack.
     * @author Griefed
     */
    public ServerPack() {

    }

    /**
     * Constructor for our ServerPack using a project and file ID from a CurseForge project.
     * @author Griefed
     * @param projectID Integer. The project ID of the CurseForge project.
     * @param fileID Integer. The file ID of the CurseForge project file.
     */
    public ServerPack(int projectID, int fileID) {
        this.projectID = projectID;
        this.fileID = fileID;
        this.projectName = "";
        this.fileName = "";
        this.fileDiskName = "";
        this.size = 0;
        this.downloads = 0;
        this.confirmedWorking = 0;
        this.status = "Queued";
    }

    /**
     * Constructor for our ServerPack, setting id, projectID, fileID, fileName, displayName, size, downloads, confirmedWorking, status, dateCreated, lastModified manually.
     * @author Griefed
     * @param id The ID of the server pack in our database.
     * @param projectID The project ID of the CurseForge project.
     * @param fileID The file ID of the CurseForge project file.
     * @param fileName The disk name of the CurseForge project file.
     * @param displayName The display name of the CurseForge project file.
     * @param size The size of the generated server pack, in MB.
     * @param downloads The amount of times this server pack was downloaded.
     * @param confirmedWorking The amount of votes indicating whether this server pack works.
     * @param status The status of this server pack. Either <code>Queued</code>, <code>Generating</code>, <code>Available</code>.
     * @param dateCreated The date and time at which this server pack was requested for generation.
     * @param lastModified The date and time this server pack was last modified. Be it either due to regeneration or something else.
     */
    public ServerPack(int id, int projectID, int fileID, String fileName, String displayName, double size, int downloads, int confirmedWorking, String status, Timestamp dateCreated, Timestamp lastModified) {
        this.id = id;
        this.projectID = projectID;
        this.fileID = fileID;
        this.fileName = fileName;
        this.fileDiskName = displayName;
        this.size = size;
        this.downloads = downloads;
        this.confirmedWorking = confirmedWorking;
        this.status = status;
        this.dateCreated = dateCreated;
        this.lastModified = lastModified;
    }

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    int id;

    // TODO: Expand with foreign key pointing towards project_table
    @Column
    int projectID;

    // TODO: Expand with foreign key pointing towards file_table
    @Column
    int fileID;

    // TODO: Move to project_table
    @Column
    String projectName;

    // TODO: Move to file_table
    @Column
    String fileName;

    @Column
    String fileDiskName;

    @Column
    double size;

    @Column
    int downloads;

    @Column
    int confirmedWorking;

    @Column
    String status;

    @Column
    String path;

    @CreationTimestamp
    @Column(updatable = false)
    Timestamp dateCreated;

    @UpdateTimestamp
    Timestamp lastModified;

    /**
     * Getter for the database id of a server pack.
     * @author Griefed
     * @return Integer. Returns the database of a server pack.
     */
    public int getId() {
        return id;
    }

    /**
     * Setter for the database id of a server pack.
     * @author Griefed
     * @param id Integer. The database id of the server pack.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Getter for the project id of the server pack.
     * @author Griefed
     * @return Integer. Returns the project id with which the server pack was generated.
     */
    @Override
    public int getProjectID() {
        return projectID;
    }

    /**
     * Setter for the project id of the server pack.
     * @author Griefed
     * @param projectID Integer. The project id with which the server pack was generated.
     */
    @Override
    public void setProjectID(int projectID) {
        this.projectID = projectID;
    }

    /**
     * Getter for the file id of the server pack.
     * @author Griefed
     * @return Integer. Returns the file id with which the server pack was generated.
     */
    @Override
    public int getFileID() {
        return fileID;
    }

    /**
     * Setter for the file id of the server pack.
     * @author Griefed
     * @param fileID Integer. The file id with which the server pack was generated.
     */
    @Override
    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

    /**
     * Getter for the name of the project of the server pack.
     * @author Griefed
     * @return String. The project name of the server pack.
     */
    @Override
    public String getProjectName() {
        return this.projectName;
    }

    /**
     * Setter for the project name of the project of the server pack.
     * @author Griefed
     * @param projectName String. The project name of the server pack.
     */
    @Override
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    /**
     * Getter for the file display name of the project file from which the server pack was generated.
     * @author Griefed
     * @return String. Returns the file display name of the project file from which the server pack was generated.
     */
    @Override
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter for the file display name of the project file from which the server pack was generated.
     * @author Griefed
     * @param fileName String. The file display name of the project file from which the server pack was generated.
     */
    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Getter for the file disk name of the project file from which the server pack was generated.
     * @author Griefed
     * @return String. The file disk name of the project file from which the server pack was generated.
     */
    @Override
    public String getFileDiskName() {
        return fileDiskName;
    }

    /**
     * Setter for the file disk name of the project file from which the server pack was generated.
     * @author Griefed
     * @param fileDiskName String. The file disk name of the project file from which the server pack was generated.
     */
    @Override
    public void setFileDiskName(String fileDiskName) {
        this.fileDiskName = fileDiskName;
    }

    /**
     * Getter for the size of the generated server pack in MB.
     * @author Griefed
     * @return Double. Returns the size of the generated server pack in MB.
     */
    public double getSize() {
        return size;
    }

    /**
     * Setter for the size of the generated server pack in MB.
     * @author Griefed
     * @param size Double. The size of the generated server pack in MB.
     */
    public void setSize(double size) {
        this.size = size;
    }

    /**
     * Getter for the amount of downloads this server pack has received.
     * @author Griefed
     * @return Integer. Returns the amount of downloads this server pack has received.
     */
    public int getDownloads() {
        return downloads;
    }

    /**
     * Setter for the amount of downloads this server pack has received.
     * @author Griefed
     * @param downloads Integer. The amount of downloads this server pack has received.
     */
    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    /**
     * Getter for the amount of votes indicating whether this server pack works. Positive values indicate a working server pack.
     * Negative values indicate the server pack is not working.
     * @author Griefed
     * @return Integer. Returns the amount of votes indicating whether this server pack works.
     */
    public int getConfirmedWorking() {
        return confirmedWorking;
    }

    /**
     * Setter for the amount of votes indicating whether this server pack works. Positive values indicate a working server pack.
     * Negative values indicate the server pack is not working.
     * @author Griefed
     * @param confirmedWorking Integer. The amount of votes indicating whether this server pack works.
     */
    public void setConfirmedWorking(int confirmedWorking) {
        this.confirmedWorking = confirmedWorking;
    }

    /**
     * Getter for the status of the server pack. Either <code>Queued</code>, <code>Generating</code>, <code>Available</code>.
     * @author Griefed
     * @return String. Returns the status of a server pack.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter for the status of a server pack. Either <code>Queued</code>, <code>Generating</code>, <code>Available</code>.
     * @author Griefed
     * @param status String. The status of a server pack.
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Getter for the path to the generated server pack.
     * @author Griefed
     * @return String. Returns the path to the generated server pack.
     */
    public String getPath() { return path; }

    /**
     * Setter for the path to the generated server pack.
     * @author Griefed
     * @param path String. The path to the generated server pack.
     */
    public void setPath(String path) { this.path = path; }

    /**
     * Getter for the date and time at which this server pack entry was created as a {@link Timestamp}.
     * @author Griefed
     * @return {@link Timestamp}. Returns the date and time at which this server pack entry was created as a {@link Timestamp}.
     */
    public Timestamp getDateCreated() {
        return dateCreated;
    }

    /**
     * Setter for the date and time at which this server pack entry was created as a {@link Timestamp}.
     * @author Griefed
     * @param dateCreated {@link Timestamp}. The date and time at which this server pack was created as a {@link Timestamp}.
     */
    public void setDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * Getter for the date and time at which this server pack entry was last modified as a {@link Timestamp}.
     * @author Griefed
     * @return {@link Timestamp}. Returns the date and time at which this server pack entry was last modified as a {@link Timestamp}.
     */
    public Timestamp getLastModified() {
        return lastModified;
    }

    /**
     * Setter for the date and time at which this server pack entry was last modified as a {@link Timestamp}.
     * @author Griefed
     * @param lastModified {@link Timestamp}. The date and time at which this server pack entry was last modified as a {@link Timestamp}.
     */
    public void setLastModified(Timestamp lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * String concatenation of all important values of our server pack entry.
     * @author Griefed
     * @return String. Returns all important information of a server pack entry as a concatenated string.
     */
    public String repositoryToString() {
        return "ServerPack{" +
                "id=" + id +
                ", projectID=" + projectID +
                ", fileID=" + fileID +
                ", projectName='" + projectName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileDiskName='" + fileDiskName + '\'' +
                ", size=" + size +
                ", downloads=" + downloads +
                ", confirmedWorking=" + confirmedWorking +
                ", status='" + status + '\'' +
                ", dateCreated=" + dateCreated +
                ", lastModified=" + lastModified +
                '}';
    }
}