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
package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.registry.ContentClass;

/**
 * A Shadow Repository is a special repository type that usually points to a master repository and transforms it in some
 * way (look at Maven1 to Maven2 layout changing repo).
 * 
 * @author cstamas
 */
@RepositoryType( pathPrefix = "shadows" )
public interface ShadowRepository
    extends Repository
{
    /**
     * The content class that is expected to have the repository set as master for this ShadowRepository.
     * 
     * @return
     */
    ContentClass getMasterRepositoryContentClass();

    /**
     * Returns the master repository of this ShadowRepository.
     * 
     * @return
     */
    String getMasterRepositoryId();

    /**
     * Sets the master repository of this ShadowRepository.
     * 
     * @param masterRepository
     * @throws IncompatibleMasterRepositoryException
     */
    void setMasterRepositoryId( String masterRepositoryId )
        throws NoSuchRepositoryException, IncompatibleMasterRepositoryException;

    /**
     * Gets sync at startup.
     * 
     * @return
     */
    boolean isSynchronizeAtStartup();

    /**
     * Sets sync at start.
     * 
     * @param value
     */
    void setSynchronizeAtStartup( boolean value );

    /**
     * Returns the master.
     * 
     * @return
     */
    Repository getMasterRepository();

    /**
     * Sets the master.
     * 
     * @return
     */
    void setMasterRepository( Repository repository )
        throws IncompatibleMasterRepositoryException;

    /**
     * Triggers syncing with master repository.
     */
    void synchronizeWithMaster();

    /**
     * Performs some activity if the event is coming from it's master repository. Implementation should filter and take
     * care what repository is the origin of the event, and simply discard event if not interested in it.
     * 
     * @param evt
     * @since 1.10.0
     */
    void onRepositoryItemEvent( RepositoryItemEvent evt );
}
