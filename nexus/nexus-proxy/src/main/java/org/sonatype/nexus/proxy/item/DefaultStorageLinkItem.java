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
package org.sonatype.nexus.proxy.item;

import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.router.RepositoryRouter;

/**
 * The Class DefaultStorageLinkItem.
 */
public class DefaultStorageLinkItem
    extends AbstractStorageItem
    implements StorageLinkItem
{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 4494595788515460394L;

    /** The target. */
    private String target;

    /**
     * Instantiates a new default storage link item.
     * 
     * @param repository the repository
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     * @param targetUid the target uid
     */
    public DefaultStorageLinkItem( Repository repository, String path, boolean canRead, boolean canWrite,
        String targetUid )
    {
        super( repository, path, canRead, canWrite );
        this.target = targetUid;
    }

    /**
     * Instantiates a new default storage link item.
     * 
     * @param router the router
     * @param path the path
     * @param canRead the can read
     * @param canWrite the can write
     * @param targetUid the target uid
     */
    public DefaultStorageLinkItem( RepositoryRouter router, String path, boolean canRead, boolean canWrite,
        String targetUid )
    {
        super( router, path, canRead, canWrite );
        this.target = targetUid;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.item.StorageLinkItem#getTarget()
     */
    public String getTarget()
    {
        return target;
    }

    /**
     * Sets the target.
     * 
     * @param target the new target
     */
    public void setTarget( String target )
    {
        this.target = target;
    }

    public void overlay( StorageItem item )
        throws IllegalArgumentException
    {
        super.overlay( item );
        setTarget( ( (StorageLinkItem) item ).getTarget() );
    }

}
