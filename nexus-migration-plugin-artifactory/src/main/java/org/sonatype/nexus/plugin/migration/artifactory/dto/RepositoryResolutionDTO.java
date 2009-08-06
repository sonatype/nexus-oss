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
package org.sonatype.nexus.plugin.migration.artifactory.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "repositoryResolution" )
public class RepositoryResolutionDTO
{
    private boolean alreadyExists;

    private boolean copyCachedArtifacts = true;

    private boolean isImport = true;

    private boolean isMixed = false;

    private boolean mapUrls = true;

    private boolean mergeSimilarRepository = false;

    private String mixResolution = EMixResolution.BOTH.name();

    private String repositoryId;

    private String similarRepositoryId;

    private String type;

    public RepositoryResolutionDTO()
    {
        super();
    }

    public EMixResolution getMixResolution()
    {
        return EMixResolution.valueOf( mixResolution );
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    public String getSimilarRepositoryId()
    {
        return similarRepositoryId;
    }

    public ERepositoryType getType()
    {
        return ERepositoryType.valueOf( this.type );
    }

    public boolean isAlreadyExists()
    {
        return alreadyExists;
    }

    public boolean isCopyCachedArtifacts()
    {
        return copyCachedArtifacts;
    }

    public final boolean isImport()
    {
        return isImport;
    }

    public boolean isMapUrls()
    {
        return mapUrls;
    }

    public boolean isMergeSimilarRepository()
    {
        return mergeSimilarRepository;
    }

    public boolean isMixed()
    {
        return isMixed;
    }

    public void setAlreadyExists( boolean alreadyExists )
    {
        this.alreadyExists = alreadyExists;
    }

    public void setCopyCachedArtifacts( boolean copyCachedArtifacts )
    {
        this.copyCachedArtifacts = copyCachedArtifacts;
    }

    public final void setImport( boolean isImport )
    {
        this.isImport = isImport;
    }

    public void setMapUrls( boolean mapUrls )
    {
        this.mapUrls = mapUrls;
    }

    public void setMergeSimilarRepository( boolean mergeSimilarRepository )
    {
        this.mergeSimilarRepository = mergeSimilarRepository;
    }

    public void setMixed( boolean isMixed )
    {
        this.isMixed = isMixed;
    }

    public void setMixResolution( EMixResolution mixResolution )
    {
        this.mixResolution = mixResolution.name();
    }

    public void setRepositoryId( String repositoryId )
    {
        this.repositoryId = repositoryId;
    }

    public void setSimilarRepositoryId( String similarRepositoryId )
    {
        this.similarRepositoryId = similarRepositoryId;
    }

    public void setType( ERepositoryType type )
    {
        this.type = type.name();
    }

}
