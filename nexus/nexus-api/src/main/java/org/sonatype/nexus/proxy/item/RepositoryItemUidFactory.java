/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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
package org.sonatype.nexus.proxy.item;

import java.util.Map;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;

public interface RepositoryItemUidFactory
{
    /**
     * Creates an UID based on a Repository reference and a path.
     * 
     * @param repository
     * @param path
     * @return
     */
    RepositoryItemUid createUid( Repository repository, String path );

    /**
     * Parses an "uid string representation" and creates an UID for it. Uid String representation is of form '<repoId> +
     * ':' + <path>'.
     * 
     * @param uidStr
     * @return
     * @throws IllegalArgumentException
     * @throws NoSuchRepositoryException
     */
    public RepositoryItemUid createUid( String uidStr )
        throws IllegalArgumentException, NoSuchRepositoryException;

    /**
     * Returns a snapshot of the active UID maps. Keys are UID string representations, while values are actual UIDs.
     * 
     * @return
     */
    Map<String, RepositoryItemUid> getActiveUidMapSnapshot();
}
