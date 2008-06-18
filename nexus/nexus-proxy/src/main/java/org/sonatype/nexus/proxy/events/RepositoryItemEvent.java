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

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * The Class RepositoryItemEvent.
 */
public abstract class RepositoryItemEvent
    extends RepositoryEvent
{

    /** The uid. */
    private final RepositoryItemUid uid;

    private Map<String, Object> context;

    /**
     * Instantiates a new repository item event.
     * 
     * @param repository the repository
     * @param uid the uid
     */
    public RepositoryItemEvent( final RepositoryItemUid uid, final Map<String, Object> context )
    {
        super( uid.getRepository() );
        this.uid = uid;
        this.context = context;
    }

    /**
     * Gets the item uid.
     * 
     * @return the item uid
     */
    public RepositoryItemUid getItemUid()
    {
        return uid;
    }

    /**
     * Gets the item context.
     * 
     * @return the item context
     */
    public Map<String, Object> getContext()
    {
        if ( context == null )
        {
            context = new HashMap<String, Object>();
        }
        
        return context;
    }

}
