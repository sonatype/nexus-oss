/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype, Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
package org.sonatype.nexus.artifact;

public class Gav
{
    private String groupId;

    private String artifactId;
    
    // TODO
    private String baseVersion;

    private String version;

    private String classifier;
    
    // TODO
    private String type;
    
    private String extension;

    private String snapshotBuildNumber;

    private Long snapshotTimeStamp;

    private String name;

    private boolean primary;

    private boolean snapshot;

    private boolean checksum;

    public Gav( String groupId, String artifactId, String version, String classifier, String extension, String snapshotBuildNumber,
        Long snapshotTimeStamp, String name, boolean primary, boolean snapshot, boolean checksum )
    {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.classifier = classifier;
        this.extension = extension;
        this.snapshotBuildNumber = snapshotBuildNumber;
        this.snapshotTimeStamp = snapshotTimeStamp;
        this.name = name;
        this.primary = primary;
        this.snapshot = snapshot;
        this.checksum = checksum;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public String getClassifier()
    {
        return classifier;
    }
    
    public String getExtension()
    {
        return extension;
    }

    public String getName()
    {
        return name;
    }

    public boolean isPrimary()
    {
        return primary;
    }

    public boolean isSnapshot()
    {
        return snapshot;
    }

    public String getSnapshotBuildNumber()
    {
        return snapshotBuildNumber;
    }

    public boolean isChecksum()
    {
        return checksum;
    }

    public Long getSnapshotTimeStamp()
    {
        return snapshotTimeStamp;
    }

}