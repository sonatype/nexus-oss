package org.sonatype.nexus.gwt.client.nexus;

import org.sonatype.gwt.client.callback.EntityRequestCallback;
import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.resource.Variant;
import org.sonatype.nexus.gwt.client.Nexus;
import org.sonatype.nexus.gwt.client.services.FeedsService;

public class DefaultFeedsService
    extends AbstractNexusService
    implements FeedsService
{

    public DefaultFeedsService( Nexus nexus, String path )
    {
        super( nexus, path );
    }

    public void listFeeds( EntityResponseHandler handler )
    {
        get( new EntityRequestCallback( handler ), getNexus().getDefaultVariant() );
    }

    public void readFeed( String path, EntityResponseHandler handler )
    {
        // TODO Auto-generated method stub
    }

    public void readFeed( String path, Variant variant, EntityResponseHandler handler )
    {
        // TODO Auto-generated method stub
    }

}
