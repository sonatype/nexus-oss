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
package org.sonatype.nexus.feeds;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.feeds.record.NexusItemInfo;

/**
 * A class thet encapsulates a Nexus artifact event: caching, deploying, deleting or retrieving of it.
 * 
 * @author cstamas
 */
public class NexusArtifactEvent
    extends AbstractEvent
{
    public static final String ACTION_CACHED = "cached";

    public static final String ACTION_DEPLOYED = "deployed";

    public static final String ACTION_DELETED = "deleted";

    public static final String ACTION_RETRIEVED = "retrieved";

    public static final String ACTION_BROKEN = "broken";

    public static final String ACTION_BROKEN_WRONG_REMOTE_CHECKSUM = "brokenWRC";

    public static final String ACTION_BROKEN_INVALID_CONTENT = "brokenIC";

    /**
     * The artifactInfo about artifact.
     */
    private final NexusItemInfo nexusItemInfo;

    /**
     * The attributes of the item in question (if any or available).
     */
    private final Map<String, String> itemAttributes;

    public NexusArtifactEvent( final Date eventDate, final String action, final String message,
                               final NexusItemInfo nexusItemInfo )
    {
        super( eventDate, action, message );

        this.nexusItemInfo = nexusItemInfo;

        this.itemAttributes = new HashMap<String, String>();
    }

    public NexusItemInfo getNexusItemInfo()
    {
        return nexusItemInfo;
    }

    public Map<String, String> getItemAttributes()
    {
        return itemAttributes;
    }

    public void addItemAttributes( Map<String, String> atr )
    {
        getItemAttributes().putAll( atr );
    }
}
