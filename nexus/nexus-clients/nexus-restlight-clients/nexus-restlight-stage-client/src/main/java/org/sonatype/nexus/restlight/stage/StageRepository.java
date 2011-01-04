/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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

    private String ipAddress;

    private String userAgent;

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
        return ( url == null ? "(No URI)" : url ) + "\n[profile: '" + profileId + "', repository: '" + repositoryId
            + "', open? " + isOpen + "]";
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

    public String getIpAddress()
    {
        return ipAddress;
    }

    public StageRepository setIpAddress( String ipAddress )
    {
        this.ipAddress = ipAddress;
        return this;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public StageRepository setUserAgent( String userAgent )
    {
        this.userAgent = userAgent;
        return this;
    }

}
