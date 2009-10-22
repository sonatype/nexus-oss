/*
 * Nexus: RESTLight Client
 * Copyright (C) 2009 Sonatype, Inc.                                                                                                                          
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
package org.sonatype.nexus.restlight.stage;

/**
 * Simple container for details of a staging repository. This minimal model is used in queries for staging-repository
 * information, and for feeding back into finish/drop/promote actions within the {@link StageClient}.
 */
public class StageRepository
{

    private final String profileId;

    private final String repositoryId;

    private String url;
    
    private String profileName;
    
    private String description;

    private final boolean isOpen;

    private String user;

    public StageRepository( final String profileId, final String repositoryId, final boolean isOpen )
    {
        this.profileId = profileId;
        this.repositoryId = repositoryId;
        this.isOpen = isOpen;
    }

    public boolean isOpen()
    {
        return isOpen;
    }

    public String getProfileId()
    {
        return profileId;
    }

    public String getRepositoryId()
    {
        return repositoryId;
    }

    @Override
    public String toString()
    {
        return ( url == null ? "(No URI)" : url ) + "\n[profile: '" + profileId + "', repository: '" + repositoryId + "', open? " + isOpen + "]";
    }

    /**
     * Return the publicly-available repository URL for this repository. This is the URL that Maven and other clients
     * would use to access artifacts in this repository.
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Set the publicly-available repository URL for this repository. This is the URL that Maven and other clients would
     * use to access artifacts in this repository.
     */
    public StageRepository setUrl( final String url )
    {
        this.url = url;
        return this;
    }

    public StageRepository setUser( final String user )
    {
        this.user = user;
        return this;
    }

    public String getUser()
    {
        return user;
    }

    public String getProfileName()
    {
        return profileName;
    }

    public StageRepository setProfileName( final String profileName )
    {
        this.profileName = profileName;
        return this;
    }

    public String getDescription()
    {
        return description;
    }

    public StageRepository setDescription( final String description )
    {
        this.description = description;
        return this;
    }

}
