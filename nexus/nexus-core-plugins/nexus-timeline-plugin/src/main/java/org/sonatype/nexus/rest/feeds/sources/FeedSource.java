/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.rest.feeds.sources;

import java.io.IOException;
import java.util.Map;

import javax.inject.Singleton;

import org.sonatype.plugin.ExtensionPoint;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * A Feed source. Impementors of this interface produces feeds.
 * 
 * @author cstamas
 */
@ExtensionPoint
@Singleton
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
