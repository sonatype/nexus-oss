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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.feeds.SystemEvent;

/**
 * The system changes feed.
 * 
 * @author cstamas
 */
@Component( role = FeedSource.class, hint = "systemRepositoryStatusChanges" )
public class SystemRepositoryStatusChangesFeedSource
    extends AbstractSystemFeedSource
{
    public static final String CHANNEL_KEY = "systemRepositoryStatusChanges";

    public List<SystemEvent> getEventList( Integer from, Integer count, Map<String, String> params )
    {
        return getFeedRecorder().getSystemEvents(
            new HashSet<String>( Arrays.asList( FeedRecorder.SYSTEM_REPO_LSTATUS_CHANGES_ACTION,
                FeedRecorder.SYSTEM_REPO_PSTATUS_CHANGES_ACTION, FeedRecorder.SYSTEM_REPO_PSTATUS_AUTO_CHANGES_ACTION ) ),
            from, count, null );
    }

    public String getFeedKey()
    {
        return CHANNEL_KEY;
    }

    public String getFeedName()
    {
        return getDescription();
    }

    @Override
    public String getDescription()
    {
        return "Repository Status Changes in Nexus (user interventions and automatic).";
    }

    @Override
    public String getTitle()
    {
        return "Repository Status Changes";
    }

}
