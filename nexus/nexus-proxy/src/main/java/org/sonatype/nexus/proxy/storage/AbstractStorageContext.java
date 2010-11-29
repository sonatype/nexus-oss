/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.storage;

import java.util.HashMap;

/**
 * The abstract storage context.
 * 
 * @author cstamas
 */
public abstract class AbstractStorageContext
    implements StorageContext
{
    private final HashMap<String, Object> context = new HashMap<String, Object>();

    private long lastChanged = System.currentTimeMillis();

    private StorageContext parent;

    public AbstractStorageContext( StorageContext parent )
    {
        setParentStorageContext( parent );
    }

    public long getLastChanged()
    {
        if ( parent != null )
        {
            return parent.getLastChanged() > lastChanged ? parent.getLastChanged() : lastChanged;
        }
        else
        {
            return lastChanged;
        }
    }

    protected void setLastChanged( long ts )
    {
        lastChanged = ts;
    }

    public StorageContext getParentStorageContext()
    {
        return parent;
    }

    public void setParentStorageContext( StorageContext parent )
    {
        this.parent = parent;
    }

    public Object getContextObject( String key )
    {
        return getContextObject( key, true );
    }

    public Object getContextObject( final String key, final boolean fallbackToParent )
    {
        if ( context.containsKey( key ) )
        {
            return context.get( key );
        }
        else if ( fallbackToParent && parent != null )
        {
            return parent.getContextObject( key );
        }
        else
        {
            return null;
        }
    }

    public void putContextObject( String key, Object value )
    {
        context.put( key, value );

        setLastChanged( System.currentTimeMillis() );
    }

    public void removeContextObject( String key )
    {
        context.remove( key );

        setLastChanged( System.currentTimeMillis() );
    }

    public boolean hasContextObject( String key )
    {
        return context.containsKey( key );
    }
}
