/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.rest.feeds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.restlet.data.MediaType;
import org.restlet.resource.StreamRepresentation;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * A restlet.org representation that represents an RSS feed. Representations are instantaniated per request, hance the
 * RssConverter should be stored somewhere to be reusable (like in restlet Context) since it is thread-safe.
 * 
 * @author cstamas
 */
public class FeedRepresentation
    extends StreamRepresentation
{
    public static final MediaType RSS_MEDIA_TYPE = new MediaType( "application/rss+xml", "RSS syndication documents" );

    public static final MediaType ATOM_MEDIA_TYPE = MediaType.APPLICATION_ATOM_XML;

    private SyndFeed feed;

    public FeedRepresentation( MediaType mediaType, SyndFeed feed )
    {
        super( mediaType );

        this.feed = feed;
    }

    @Override
    public InputStream getStream()
        throws IOException
    {
        return null;
    }

    public void write( OutputStream outputStream )
        throws IOException
    {
        try
        {
            Writer w = new OutputStreamWriter( outputStream );

            SyndFeedOutput output = new SyndFeedOutput();

            output.output( feed, w );
        }
        catch ( FeedException e )
        {
            throw new RuntimeException( "Got exception while generating feed!", e );
        }
    }
}
