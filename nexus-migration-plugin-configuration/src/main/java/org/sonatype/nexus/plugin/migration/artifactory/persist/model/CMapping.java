/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugin.migration.artifactory.persist.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "mapping" )
public class CMapping
{

    private String artifactoryRepositoryId;

    private String nexusGroupId;

    private String nexusRepositoryId;

    private String releasesRepositoryId;

    private String snapshotsRepositoryId;

    public CMapping()
    {
        super();
    }

    public CMapping( String artifactoryRepositoryId, String nexusRepositoryId )
    {
        this();
        this.artifactoryRepositoryId = artifactoryRepositoryId;
        this.nexusRepositoryId = nexusRepositoryId;
    }

    public CMapping( String artifactoryRepositoryId, String nexusGroupId, String releasesRepositoryId,
                     String snapshotsRepositoryId )
    {
        this();
        this.artifactoryRepositoryId = artifactoryRepositoryId;
        this.nexusGroupId = nexusGroupId;
        this.releasesRepositoryId = releasesRepositoryId;
        this.snapshotsRepositoryId = snapshotsRepositoryId;
    }

    public String getArtifactoryRepositoryId()
    {
        return artifactoryRepositoryId;
    }

    public String getNexusGroupId()
    {
        return nexusGroupId;
    }

    public String getNexusRepositoryId()
    {
        return nexusRepositoryId;
    }

    public void setArtifactoryRepositoryId( String artifactoryRepositoryId )
    {
        this.artifactoryRepositoryId = artifactoryRepositoryId;
    }

    public void setNexusGroupId( String nexusGroupId )
    {
        this.nexusGroupId = nexusGroupId;
    }

    public void setNexusRepositoryId( String nexusRepositoryId )
    {
        this.nexusRepositoryId = nexusRepositoryId;
    }

    public String getReleasesRepositoryId()
    {
        return releasesRepositoryId;
    }

    public void setReleasesRepositoryId( String releasesRepositoryId )
    {
        this.releasesRepositoryId = releasesRepositoryId;
    }

    public String getSnapshotsRepositoryId()
    {
        return snapshotsRepositoryId;
    }

    public void setSnapshotsRepositoryId( String snapshotsRepositoryId )
    {
        this.snapshotsRepositoryId = snapshotsRepositoryId;
    }

}
