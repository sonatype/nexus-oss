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
package org.sonatype.nexus.obr.metadata;

import org.osgi.service.obr.Resource;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageFileItem;

/**
 * Component that provides methods to read, generate, and write OBR metadata.
 */
public interface ObrMetadataSource
{
    /**
     * Gets an OBR reader for the given site.
     * 
     * @param site the OBR site
     * @return the resource reader
     * @throws StorageException
     */
    ObrResourceReader getReader( ObrSite site )
        throws StorageException;

    /**
     * Builds an OBR resource for the given repository item, null if the item is not an OSGi bundle.
     * 
     * @param item the bundle item
     * @return a new resource, null if not a bundle
     */
    Resource buildResource( StorageFileItem item );

    /**
     * Gets an OBR writer for the given repository item.
     * 
     * @param uid the target UID
     * @return the resource writer
     * @throws StorageException
     */
    ObrResourceWriter getWriter( RepositoryItemUid uid )
        throws StorageException;
}
