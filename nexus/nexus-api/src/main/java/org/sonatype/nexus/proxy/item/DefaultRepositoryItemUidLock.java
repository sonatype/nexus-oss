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

import org.sonatype.nexus.proxy.access.Action;

public class DefaultRepositoryItemUidLock
    implements RepositoryItemUidLock
{
    private final String key;

    private final LockResource contentLock;

    protected DefaultRepositoryItemUidLock( final String key, final LockResource contentLock )
    {
        super();

        this.key = key;

        this.contentLock = contentLock;
    }

    @Override
    public void lock( final Action action )
    {
        if ( action.isReadAction() )
        {
            contentLock.lockShared();
        }
        else
        {
            contentLock.lockExclusively();
        }
    }

    @Override
    public void unlock()
    {
        contentLock.unlock();
    }

    // ==

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( key == null ) ? 0 : key.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        DefaultRepositoryItemUidLock other = (DefaultRepositoryItemUidLock) obj;
        if ( key == null )
        {
            if ( other.key != null )
                return false;
        }
        else if ( !key.equals( other.key ) )
            return false;
        return true;
    }

    // for Debug/tests vvv

    protected LockResource getContentLock()
    {
        return contentLock;
    }

    // for Debug/tests ^^^

}
