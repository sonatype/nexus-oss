package org.sonatype.nexus.gwt.client.nexus;

import org.sonatype.gwt.client.callback.EntityRequestCallback;
import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.handler.StatusResponseHandler;
import org.sonatype.gwt.client.resource.PathUtils;
import org.sonatype.gwt.client.resource.Representation;
import org.sonatype.nexus.gwt.client.Nexus;
import org.sonatype.nexus.gwt.client.services.RepositoriesService;
import org.sonatype.nexus.gwt.client.services.RepositoryService;

public class DefaultRepositoriesService
    extends AbstractNexusService
    implements RepositoriesService
{

    public DefaultRepositoriesService( Nexus nexus, String path )
    {
        super( nexus, path );
    }

    public void getRepositories( EntityResponseHandler handler )
    {
        get( new EntityRequestCallback( handler ), getNexus().getDefaultVariant() );
    }

    public RepositoryService getRepositoryById( String id )
    {
        return getRepositoryByPath( PathUtils.append( getPath(), id ) );
    }

    public RepositoryService getRepositoryByPath( String path )
    {
        return new DefaultRepositoryService( getNexus(), path );
    }

    public RepositoryService createRepository( String id, Representation representation, StatusResponseHandler handler )
    {
        RepositoryService repository = getRepositoryById( id );

        repository.create( representation, handler );

        return repository;
    }

}
