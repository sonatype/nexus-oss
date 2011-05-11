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
package org.sonatype.nexus.proxy.item.uid;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.slf4j.Logger;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;

/**
 * This is just a quick implementation for currently only one existing attribute: is hidden. Later this should be
 * extended.
 * 
 * @author cstamas
 */
@Component( role = RepositoryItemUidAttributeManager.class )
public class DefaultRepositoryItemUidAttributeManager
    implements RepositoryItemUidAttributeManager
{
    @Requirement
    private Logger logger;

    @Requirement( role = RepositoryItemUidAttributeSource.class )
    private Map<String, RepositoryItemUidAttributeSource> attributeSources;

    private final Map<Class<?>, Attribute<?>> attributes;

    public DefaultRepositoryItemUidAttributeManager()
    {
        this.attributes = new ConcurrentHashMap<Class<?>, Attribute<?>>();
    }

    @SuppressWarnings( "unchecked" )
    public <T extends Attribute<?>> T getAttribute( final Class<T> attributeKey, final RepositoryItemUid subject )
    {
        return (T) attributes.get( attributeKey );
    }

    public void reset()
    {
        attributes.clear();

        final ArrayList<String> sources = new ArrayList<String>( attributeSources.size() );

        for ( Map.Entry<String, RepositoryItemUidAttributeSource> attributeSourceEntry : attributeSources.entrySet() )
        {
            sources.add( attributeSourceEntry.getKey() );

            Map<Class<?>, Attribute<?>> attrs = attributeSourceEntry.getValue().getAttributes();

            if ( attrs != null )
            {
                attributes.putAll( attrs );
            }
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Registered {} UID Attributes coming from following sources: {}",
                new Object[] { attributes.size(), sources.toString() } );
        }
    }
}
