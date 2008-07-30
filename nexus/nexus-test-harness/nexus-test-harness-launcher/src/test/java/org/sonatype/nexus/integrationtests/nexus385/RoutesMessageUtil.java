package org.sonatype.nexus.integrationtests.nexus385;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.StringUtils;
import org.restlet.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.sonatype.nexus.configuration.model.CGroupsSettingPathMappingItem;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.rest.model.NexusError;
import org.sonatype.nexus.rest.model.NexusErrorResponse;
import org.sonatype.nexus.rest.model.RepositoryRouteMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryRouteResource;
import org.sonatype.nexus.rest.model.RepositoryRouteResourceResponse;
import org.sonatype.nexus.test.utils.NexusConfigUtil;
import org.sonatype.plexus.rest.representation.XStreamRepresentation;

import com.thoughtworks.xstream.XStream;

public class RoutesMessageUtil
{
    private XStream xstream;

    private MediaType mediaType;

    private String baseNexusUrl;

    public RoutesMessageUtil( XStream xstream, MediaType mediaType, String baseNexusUrl )
    {
        super();
        this.xstream = xstream;
        this.mediaType = mediaType;
        this.baseNexusUrl = baseNexusUrl;
    }

    public Response sendMessage( Method method, RepositoryRouteResource resource )
    {
        XStreamRepresentation representation = new XStreamRepresentation( xstream, "", mediaType );

        String resourceId = ( resource.getId() == null ) ? "" : "/" + resource.getId();

        String serviceURI = this.baseNexusUrl + "service/local/repo_routes" + resourceId;
        System.out.println( "serviceURI: " + serviceURI );

        Request request = new Request();

        request.setResourceRef( serviceURI );

        request.setMethod( method );

        if ( method != Method.GET || method != Method.DELETE )
        {
            RepositoryRouteResourceResponse requestResponse = new RepositoryRouteResourceResponse();
            requestResponse.setData( resource );

            // now set the payload
            representation.setPayload( requestResponse );
            System.out.println( method.getName() + ": " + representation.getText() );
            request.setEntity( representation );
        }

        Client client = new Client( Protocol.HTTP );

        return client.handle( request );
    }

    public RepositoryRouteResource getResourceFromResponse( Response response ) throws IOException
    {
        String responseString = response.getEntity().getText();
        System.out.println( "responseText: "+ responseString );
        
        Assert.assertFalse( "Response text was empty.", StringUtils.isEmpty( responseString ) );

        XStreamRepresentation representation = new XStreamRepresentation( xstream, responseString, mediaType );

        RepositoryRouteResourceResponse resourceResponse =
            (RepositoryRouteResourceResponse) representation.getPayload( new RepositoryRouteResourceResponse() );

        return resourceResponse.getData();
    }
    
    public void validateSame( List<RepositoryRouteMemberRepository> repos1, List<RepositoryRouteMemberRepository> repos2 )
    {
        Assert.assertEquals( repos1.size(), repos2.size() );
        
        for ( int ii = 0; ii < repos1.size(); ii++ )
        {
            RepositoryRouteMemberRepository repo1 = repos1.get( ii );
            RepositoryRouteMemberRepository repo2 = repos2.get( ii );
            this.validateSame( repo1, repo2 );
        }
    }
    
    public void validateSameRepoIds( List<RepositoryRouteMemberRepository> repos1, List<String> repos2 )
    {
        Assert.assertEquals( repos1.size(), repos2.size() );
        
        // this is ordered
        for ( int ii = 0; ii < repos1.size(); ii++ )
        {
            RepositoryRouteMemberRepository repo1 = repos1.get( ii );
            String repo2 = repos2.get( ii );
            Assert.assertEquals( repo1.getId(), repo2 );
        }
    }
    
    public void validateSame( RepositoryRouteMemberRepository repo1, RepositoryRouteMemberRepository repo2 )
    {
        // we only care about the Id field
        Assert.assertEquals( repo1.getId(), repo2.getId() );
    }
    
    
    public void validateRoutesConfig( RepositoryRouteResource resource ) throws IOException
    {
        
        CGroupsSettingPathMappingItem cRoute = NexusConfigUtil.getRoute( resource.getId() );
        
        Assert.assertEquals( resource.getGroupId(), cRoute.getGroupId() );
        Assert.assertEquals( resource.getPattern(), cRoute.getRoutePattern() );
        Assert.assertEquals( resource.getRuleType(), cRoute.getRouteType() );
        
        this.validateSameRepoIds( resource.getRepositories(), cRoute.getRepositories() );

        
    }

    public void validateResponseErrorXml( String xml )
    {

        NexusErrorResponse errorResponse = (NexusErrorResponse) xstream.fromXML( xml, new NexusErrorResponse() );

        Assert.assertTrue( "Error response is empty.", errorResponse.getErrors().size() > 0 );

        for ( Iterator<NexusError> iter = errorResponse.getErrors().iterator(); iter.hasNext(); )
        {
            NexusError error = (NexusError) iter.next();
            Assert.assertFalse( "Response Error message is empty.", StringUtils.isEmpty( error.getMsg() ) );

        }

    }

}
