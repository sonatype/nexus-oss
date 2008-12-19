package org.sonatype.nexus.plugins.migration.util;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.PlexusUserListResourceResponse;
import org.sonatype.nexus.rest.model.PlexusUserResource;
import org.sonatype.nexus.test.utils.UserMessageUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class PlexusUserMessageUtil
{
    private XStream xstream;

    private MediaType mediaType;

    private static final Logger LOG = Logger.getLogger( UserMessageUtil.class );

    public PlexusUserMessageUtil( XStream xstream, MediaType mediaType )
    {
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    @SuppressWarnings( "unchecked" )
    public List<PlexusUserResource> getList()
        throws IOException
    {
        Response response = RequestFacade.doGetRequest( "service/local/plexus_users/allConfigured" );
        String responseText = response.getEntity().getText();
        LOG.debug( "responseText: \n" + responseText );

        XStreamRepresentation representation = new XStreamRepresentation(
            XStreamFactory.getXmlXStream(),
            responseText,
            MediaType.APPLICATION_XML );

        // make sure we have a success
        Assert.assertTrue( "Status: " + response.getStatus() + "\n" + responseText, response.getStatus().isSuccess() );

        PlexusUserListResourceResponse resourceResponse = (PlexusUserListResourceResponse) representation
            .getPayload( new PlexusUserListResourceResponse() );

        return resourceResponse.getData();
    }

}
