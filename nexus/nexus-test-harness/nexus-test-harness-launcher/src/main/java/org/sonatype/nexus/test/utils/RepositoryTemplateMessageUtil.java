package org.sonatype.nexus.test.utils;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.RepositoryBaseResource;
import org.sonatype.nexus.rest.model.RepositoryResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

public class RepositoryTemplateMessageUtil
{
    private static final Logger LOG = Logger.getLogger( RepositoryTemplateMessageUtil.class );

    public static final String TEMPLATE_PROXY_SNAPSHOT = "default_proxy_snapshot";

    public static final String TEMPLATE_PROXY_RELEASE = "default_proxy_release";

    public RepositoryBaseResource getTemplate( String id )
        throws IOException
    {
        Response response = RequestFacade.doGetRequest( "service/local/templates/repositories/" + id );

        String responseText = response.getEntity().getText();
        if ( response.getStatus().isError() )
        {
            Assert.fail( "Error on request: " + response.getStatus() + "\n" + responseText );
        }

        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation = new XStreamRepresentation(
            XStreamFactory.getXmlXStream(),
            responseText,
            MediaType.APPLICATION_XML );

        RepositoryResourceResponse resourceResponse = (RepositoryResourceResponse) representation
            .getPayload( new RepositoryResourceResponse() );

        return resourceResponse.getData();
    }
}
