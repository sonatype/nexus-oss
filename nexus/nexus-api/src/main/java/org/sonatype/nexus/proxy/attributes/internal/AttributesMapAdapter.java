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
package org.sonatype.nexus.proxy.attributes.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.sonatype.nexus.proxy.attributes.Attributes;
import org.sonatype.nexus.proxy.item.StorageItem;

/**
 * Adapter adapting Attributes to Map<String, String> interface for backward compatibility reasons, to make us able to
 * implement {@link StorageItem#getAttributes()}. This method is deprecated anyway, so the existence of the adapter is
 * tied to existence of that method. Note: this adapter breaks the Map contract on three methods: {@link #keySet()},
 * {@link #values()} and {@link #entrySet()}. Here, we provide an unmodifiable "view" only, and iterator modification
 * methods will fail.
 * 
 * @author cstamas
 * @since 1.10.0
 */
public class AttributesMapAdapter
    implements Map<String, String>
{
    private final Attributes attributes;

    private Map<String, String> view;

    public AttributesMapAdapter( final Attributes attributes )
    {
        this.attributes = attributes;
        resetView();
    }

    protected void resetView()
    {
        view = attributes.asMap();
    }

    @Override
    public int size()
    {
        return view.size();
    }

    @Override
    public boolean isEmpty()
    {
        return view.isEmpty();
    }

    @Override
    public boolean containsKey( Object key )
    {
        return view.containsKey( key );
    }

    @Override
    public boolean containsValue( Object value )
    {
        return view.containsValue( value );
    }

    @Override
    public String get( Object key )
    {
        return view.get( key );
    }

    @Override
    public String put( String key, String value )
    {
        final String result = attributes.put( key, value );
        resetView();
        return result;
    }

    @Override
    public String remove( Object key )
    {
        final String result = attributes.remove( String.valueOf( key ) );
        resetView();
        return result;
    }

    @Override
    public void putAll( Map<? extends String, ? extends String> m )
    {
        attributes.putAll( m );
        resetView();
    }

    @Override
    public void clear()
    {
        throw new UnsupportedOperationException( "Cannot clear() item attributes!" );
    }

    @Override
    public Set<String> keySet()
    {
        return Collections.unmodifiableSet( view.keySet() );
    }

    @Override
    public Collection<String> values()
    {
        return Collections.unmodifiableCollection( view.values() );
    }

    @Override
    public Set<java.util.Map.Entry<String, String>> entrySet()
    {
        return Collections.unmodifiableSet( view.entrySet() );
    }
}
