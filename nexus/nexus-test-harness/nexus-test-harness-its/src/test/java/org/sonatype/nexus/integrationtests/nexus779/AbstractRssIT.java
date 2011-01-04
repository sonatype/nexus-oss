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
package org.sonatype.nexus.integrationtests.nexus779;

import java.util.List;

import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.test.utils.FeedUtil;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public abstract class AbstractRssIT
    extends AbstractPrivilegeTest
{

    private static final String RECENTLY_DEPLOYED = "recentlyDeployedArtifacts";

    private List<SyndEntry> entries;

    public AbstractRssIT( String testRepositoryId )
    {
        super( testRepositoryId );
    }

    public AbstractRssIT()
    {
        super();
    }

    protected String entriesToString()
        throws Exception
    {
        if ( entries == null )
        {
            return "No entries";
        }

        StringBuffer buffer = new StringBuffer();

        for ( SyndEntry syndEntry : entries )
        {
            buffer.append( "\n" ).append( syndEntry.getTitle() );
        }

        return buffer.toString();
    }

    @SuppressWarnings( "unchecked" )
    protected boolean feedListContainsArtifact( String groupId, String artifactId, String version )
        throws Exception
    {
        SyndFeed feed = FeedUtil.getFeed( RECENTLY_DEPLOYED );
        entries = feed.getEntries();

        for ( SyndEntry entry : entries )
        {
            if ( entry.getTitle().contains( groupId ) && entry.getTitle().contains( artifactId )
                && entry.getTitle().contains( version ) )
            {
                return true;
            }
        }
        return false;
    }

}