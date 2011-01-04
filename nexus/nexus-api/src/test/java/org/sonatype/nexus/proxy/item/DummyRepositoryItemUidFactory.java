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

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.repository.Repository;

public class DummyRepositoryItemUidFactory
    implements RepositoryItemUidFactory
{
    private Map<String, DefaultRepositoryItemUid> uids = new HashMap<String, DefaultRepositoryItemUid>();

    public synchronized DefaultRepositoryItemUid createUid( Repository repository, String path )
    {
        String key = repository.getId() + ":" + path;

        if ( !uids.containsKey( key ) )
        {
            uids.put( key, new DefaultRepositoryItemUid( this, repository, path ) );
        }

        return uids.get( key );
    }

    public RepositoryItemUid createUid( String uidStr )
        throws IllegalArgumentException, NoSuchRepositoryException
    {
        throw new UnsupportedOperationException(
            "This dummy factory does not supports this method (it needs repo registry et al)" );
    }

    public Map<String, RepositoryItemUid> getActiveUidMapSnapshot()
    {
        return new HashMap<String, RepositoryItemUid>( uids );
    }
}
