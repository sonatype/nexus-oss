package org.sonatype.nexus.test.utils;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.rest.model.ContentListResource;
import org.sonatype.nexus.rest.model.ContentListResourceResponse;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class ContentListMessageUtil
{
    private static final String SERVICE_PART = "service/local/repo_groups";

    private XStream xstream;

    private MediaType mediaType;

    private static final Logger LOG = Logger.getLogger( GroupMessageUtil.class );

    public ContentListMessageUtil( XStream xstream, MediaType mediaType )
    {
        this.xstream = xstream;
        this.mediaType = mediaType;
    }

    protected Response getResponse( String repoId, String path, boolean isGroup )
        throws IOException
    {
        String groupOrRepoPart = isGroup ? "repo_groups/" : "repositories/";
        String uriPart = RequestFacade.SERVICE_LOCAL + groupOrRepoPart + repoId + "/content" + path;
        
        return RequestFacade.sendMessage( uriPart, Method.GET );
    }

    @SuppressWarnings("unchecked")
    public List<ContentListResource> getContentListResource( String repoId, String path, boolean isGroup )
        throws IOException
    {
        Response response = this.getResponse( repoId, path, isGroup );

        String responeText = response.getEntity().getText();
        Assert.assertTrue(
            "Expected sucess: Status was: " + response.getStatus() + "\nResponse:\n" + responeText,
            response.getStatus().isSuccess() );

        XStreamRepresentation representation = new XStreamRepresentation( this.xstream, responeText, this.mediaType );
        ContentListResourceResponse listRepsonse = (ContentListResourceResponse) representation
            .getPayload( new ContentListResourceResponse() );

        return listRepsonse.getData();

    }
}
