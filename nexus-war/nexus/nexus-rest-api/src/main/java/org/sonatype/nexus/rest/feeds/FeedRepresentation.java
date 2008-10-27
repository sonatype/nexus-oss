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
