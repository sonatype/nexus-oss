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
