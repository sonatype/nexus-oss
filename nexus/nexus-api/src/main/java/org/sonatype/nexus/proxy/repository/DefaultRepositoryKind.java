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
package org.sonatype.nexus.proxy.repository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DefaultRepositoryKind
    implements RepositoryKind
{
    private final Class<?> mainFacet;

    private final Set<Class<?>> facets;

    public DefaultRepositoryKind( Class<?> mainFacet, Collection<Class<?>> facets )
    {
        this.mainFacet = mainFacet;

        this.facets = new HashSet<Class<?>>();

        this.facets.add( mainFacet );

        if ( facets != null )
        {
            this.facets.addAll( facets );
        }
    }

    public Class<?> getMainFacet()
    {
        return mainFacet;
    }

    public boolean isFacetAvailable( Class<?> f )
    {
        for ( Class<?> facet : facets )
        {
            if ( f.isAssignableFrom( facet ) )
            {
                return true;
            }
        }

        return false;
    }
}
