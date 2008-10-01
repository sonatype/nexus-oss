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
package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;

/**
 * The event fired when repository's local status is changed.
 * 
 * @author cstamas
 */
public class RepositoryEventLocalStatusChanged
    extends RepositoryEvent
{
    private final LocalStatus oldLocalStatus;

    private final LocalStatus newLocalStatus;

    /**
     * Instantiates a new repository event evict unused items.
     * 
     * @param repository the repository
     */
    public RepositoryEventLocalStatusChanged( final Repository repository, final LocalStatus oldLocalStatus,
        final LocalStatus newLocalStatus )
    {
        super( repository );

        this.oldLocalStatus = oldLocalStatus;

        this.newLocalStatus = newLocalStatus;
    }

    public LocalStatus getOldLocalStatus()
    {
        return oldLocalStatus;
    }

    public LocalStatus getNewLocalStatus()
    {
        return newLocalStatus;
    }
}
