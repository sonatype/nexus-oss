/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
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
package org.sonatype.nexus.rest.feeds;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.sonatype.nexus.Nexus;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * And abstract class for NexusArtifactEvent based feeds. This class implements all needed to create a feed,
 * implementors needs only to implement 3 abtract classes.
 * 
 * @author cstamas
 */
public abstract class AbstractFeedSource
    implements FeedSource
{

    /** @plexus.requirement */
    private Nexus nexus;

    /**
     * Simple date format for the creation date ISO representation (partial).
     */
    private static final SimpleDateFormat creationDateFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss zzz" );

    /** Static initializers for the GMT time zone. */
    static
    {
        creationDateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
    }

    protected String formatDate( Date date )
    {
        return creationDateFormat.format( date );
    }

    public Nexus getNexus()
    {
        return nexus;
    }

    public void setNexus( Nexus nexus )
    {
        this.nexus = nexus;
    }

    public abstract String getTitle();

    public abstract String getDescription();

    public abstract SyndFeed getFeed();

}
