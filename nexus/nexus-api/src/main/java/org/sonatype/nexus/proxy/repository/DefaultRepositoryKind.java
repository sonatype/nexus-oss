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
