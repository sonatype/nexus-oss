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
package org.sonatype.nexus.rest.feeds.sources;

import java.io.IOException;
import java.util.Map;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * A Feed source. Impementors of this interface produces feeds.
 * 
 * @author cstamas
 */
public interface FeedSource
{
    /**
     * Returns the channel key that identifies this channel.
     * 
     * @return
     */
    String getFeedKey();

    /**
     * Returns the feed human name.
     * 
     * @return
     */
    String getFeedName();

    /**
     * Returns a Feed Channel.
     * 
     * @return a channel
     * @throws IOException
     */
    SyndFeed getFeed( Integer from, Integer count, Map<String, String> params )
        throws IOException;
}
