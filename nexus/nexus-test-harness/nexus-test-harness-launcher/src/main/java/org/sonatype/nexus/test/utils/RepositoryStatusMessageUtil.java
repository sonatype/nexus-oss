package org.sonatype.nexus.test.utils;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.rest.model.RepositoryStatusResource;
import org.sonatype.nexus.rest.model.RepositoryStatusResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class RepositoryStatusMessageUtil
{

    public static Response putOutOfService( String repoId, String repoType )
        throws IOException
    {
        RepositoryStatusResource status = new RepositoryStatusResource();
        status.setId( repoId );
        status.setRepoType( repoType );
        status.setLocalStatus( LocalStatus.OUT_OF_SERVICE.name() );
        return changeStatus( status );
    }

    public static Response putInService( String repoId, String repoType )
    throws IOException
    {
        RepositoryStatusResource status = new RepositoryStatusResource();
        status.setId( repoId );
        status.setRepoType( repoType );
        status.setLocalStatus( LocalStatus.IN_SERVICE.name() );
        return changeStatus( status );
    }

    private static Response changeStatus( RepositoryStatusResource status )
        throws IOException
    {
        String serviceURI = "service/local/repositories/" + status.getId() + "/status?undefined";

        XStreamRepresentation representation =
            new XStreamRepresentation( XStreamFactory.getXmlXStream(), "", MediaType.APPLICATION_XML );
        RepositoryStatusResourceResponse request = new RepositoryStatusResourceResponse();
        request.setData( status );
        representation.setPayload( request );

        Response response = RequestFacade.sendMessage( serviceURI, Method.PUT, representation );
        return response;

    }
}
