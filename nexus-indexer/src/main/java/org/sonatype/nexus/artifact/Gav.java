/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tamás Cservenák (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
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