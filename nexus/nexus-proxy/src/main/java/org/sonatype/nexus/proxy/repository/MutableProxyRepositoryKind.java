package org.sonatype.nexus.proxy.repository;

import java.util.Collection;
import java.util.HashSet;

public class MutableProxyRepositoryKind
    implements RepositoryKind
{
    private final ProxyRepository repository;

    private RepositoryKind hostedKind;

    private RepositoryKind proxyKind;

    private HashSet<Class<?>> sharedFacets;

    public MutableProxyRepositoryKind( ProxyRepository repository )
    {
        this( repository, null );
    }

    public MutableProxyRepositoryKind( ProxyRepository repository, Collection<Class<?>> sharedFacets )
    {
        this( repository, sharedFacets, null, null );
    }

    public MutableProxyRepositoryKind( ProxyRepository repository, Collection<Class<?>> sharedFacets,
        RepositoryKind hostedKind, RepositoryKind proxyKind )
    {
        this.repository = repository;

        this.hostedKind = hostedKind;

        this.proxyKind = proxyKind;

        this.sharedFacets = new HashSet<Class<?>>();

        if ( sharedFacets != null )
        {
            this.sharedFacets.addAll( sharedFacets );
        }
    }

    public RepositoryKind getProxyKind()
    {
        return proxyKind;
    }

    public void setProxyKind( RepositoryKind proxyKind )
    {
        this.proxyKind = proxyKind;
    }

    public RepositoryKind getHostedKind()
    {
        return hostedKind;
    }

    public void setHostedKind( RepositoryKind hostedKind )
    {
        this.hostedKind = hostedKind;
    }

    private boolean isProxy()
    {
        return repository.getRemoteUrl() != null;
    }

    private RepositoryKind getActualRepositoryKind()
    {
        if ( isProxy() )
        {
            return proxyKind;
        }
        else
        {
            return hostedKind;
        }
    }

    public Class<?> getMainFacet()
    {
        return getActualRepositoryKind().getMainFacet();
    }

    public boolean isFacetAvailable( Class<?> f )
    {
        if ( getActualRepositoryKind().isFacetAvailable( f ) )
        {
            return true;
        }

        for ( Class<?> facet : sharedFacets )
        {
            if ( f.isAssignableFrom( facet ) )
            {
                return true;
            }
        }

        return false;
    }
}
