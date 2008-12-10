/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.rest.feeds;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * Simple class that calculates the feed key by attribute (and it is probably mapped with FEED_KEY in some router).
 * 
 * @author cstamas
 * @author dip
 */
@Component( role = PlexusResource.class, hint = "feed" )
public class FeedPlexusResource
    extends AbstractFeedPlexusResource
{
    public static final String FEED_KEY = "feedKey";

    @Override
    public Object getPayloadInstance()
    {
        // RO resource, no payload
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/feeds/{" + FEED_KEY + "}";
    }

    @Override
    protected String getChannelKey( Request request )
    {
        return (String) request.getAttributes().get( FEED_KEY );
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/feeds/*", "authcBasic,perms[nexus:feeds]" );
    }

    @Override
    protected SyndFeed getFeed( Context context, Request request, String channelKey, Integer from, Integer count,
        Map<String, String> params )
        throws IOException,
            ComponentLookupException
    {
        SyndFeed feed = super.getFeed( context, request, channelKey, from, count, params );

        if ( feed.getLink() != null )
        {
            // full URLs should not be touched
            if ( !feed.getLink().startsWith( "http" ) )
            {
                if ( feed.getLink().startsWith( "/" ) )
                {
                    feed.setLink( feed.getLink().substring( 1 ) );
                }
                feed.setLink( createRootReference( request, feed.getLink() ).toString() );
            }
        }

        // TODO: A hack: creating "absolute" links
        for ( SyndEntry entry : (List<SyndEntry>) feed.getEntries() )
        {
            if ( entry.getLink() != null )
            {
                // full URLs should not be touched
                if ( !entry.getLink().startsWith( "http" ) )
                {
                    if ( entry.getLink().startsWith( "/" ) )
                    {
                        entry.setLink( entry.getLink().substring( 1 ) );
                    }
                    entry.setLink( createRootReference( request, entry.getLink() ).toString() );
                }
            }
        }

        return feed;
    }
}
