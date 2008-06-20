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

import java.util.List;

import org.sonatype.nexus.feeds.SystemEvent;

/**
 * The system changes feed.
 * 
 * @author cstamas
 * @plexus.component role-hint="systemRepositoryStatusChanges"
 */
public class SystemRepositoryStatusChangesFeedSource
    extends AbstractSystemFeedSource
{
    public static final String CHANNEL_KEY = "systemRepositoryStatusChanges";

    public List<SystemEvent> getEventList()
    {
        return getNexus().getRepositoryStatusChanges();
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
