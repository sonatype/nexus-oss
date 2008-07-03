package org.sonatype.nexus.gwt.client.nexus;

import org.sonatype.gwt.client.callback.EntityRequestCallback;
import org.sonatype.gwt.client.callback.RestRequestCallback;
import org.sonatype.gwt.client.callback.StatusRequestCallback;
import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.handler.StatusResponseHandler;
import org.sonatype.gwt.client.resource.Representation;
import org.sonatype.nexus.gwt.client.Nexus;
import org.sonatype.nexus.gwt.client.services.RepositoryService;

public class DefaultRepositoryService
    extends AbstractNexusService
    implements RepositoryService
{

    public DefaultRepositoryService( Nexus nexus, String path )
    {
        super( nexus, path );
    }

    public void create( Representation representation, StatusResponseHandler handler )
    {
        put( new StatusRequestCallback( RestRequestCallback.SUCCESS_CREATED, handler ), representation );
    }

    public void read( EntityResponseHandler handler )
    {
        get( new EntityRequestCallback( handler ), getNexus().getDefaultVariant() );
    }

    public void update( Representation representation, StatusResponseHandler handler )
    {
        // we make no distinction here, if URI is nonexistent, it is CREATE, otherwise it is UPDATE
        create( representation, handler );
    }

    public void delete( StatusResponseHandler handler )
    {
        delete( new StatusRequestCallback( handler ) );
    }

    public void readRepositoryMeta( EntityResponseHandler handler )
    {
        // TODO Auto-generated method stub

    }

    public void readRepositoryStatus( EntityResponseHandler handler )
    {
        // TODO Auto-generated method stub

    }

    public void updateRepositoryStatus( Representation representation, EntityResponseHandler handler )
    {
        // TODO Auto-generated method stub

    }

}
