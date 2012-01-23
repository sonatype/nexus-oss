/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.restlight.stage;

import java.text.SimpleDateFormat;
import java.util.Locale;

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

    private String createdDate;

    private String closedDate;

    //Nexus sends a string like: 'Fri Jul 29 12:41:40 CEST 2011' or 'n/a'
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy", Locale.US );

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
            + "', open? " + isOpen + ", created: " + ( createdDate == null ? "n/a" : createdDate ) + "]";
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

    public String getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate( final String createdDate )
    {
        this.createdDate = createdDate;
    }

    public String getClosedDate()
    {
        return closedDate;
    }

    public static SimpleDateFormat getDateFormat()
    {
        return FORMAT;
    }

    public void setClosedDate( final String closedDate )
    {
        this.closedDate = closedDate;
    }
}
