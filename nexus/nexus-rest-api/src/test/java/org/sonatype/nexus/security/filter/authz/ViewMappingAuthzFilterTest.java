package org.sonatype.nexus.security.filter.authz;

import static org.easymock.EasyMock.replay;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusConstants;
import org.easymock.EasyMock;
import org.sonatype.nexus.AbstractNexusTestCase;

public class ViewMappingAuthzFilterTest extends AbstractNexusTestCase
{

    private NexusViewMappingAuthorizationFilter getFilter()
    {
        NexusViewMappingAuthorizationFilter filter = new NexusViewMappingAuthorizationFilter();
    
        ServletContext mockServletContext = EasyMock.createNiceMock( ServletContext.class );
        EasyMock.expect( mockServletContext.getAttribute( PlexusConstants.PLEXUS_KEY ) ).andReturn( this.getContainer() ).anyTimes();
        replay( mockServletContext );
        
        filter.setServletContext( mockServletContext );
        filter.setPathPrefix( "/repositories/(.*)" );
        
        return filter;
    }
    
    public void testFilter() throws IOException
    {
//        HttpServletRequest mockRequest = EasyMock.createNiceMock( HttpServletRequest.class );
//        EasyMock.expect( mockRequest.getContextPath()).andReturn( "/app" ).anyTimes();
//        EasyMock.expect( mockRequest.getRequestURI() ).andReturn( "/app/repositories/foo" ).anyTimes();
//        replay( mockRequest );
//        
//        NexusViewMappingAuthorizationFilter filter = this.getFilter();
//        
//        boolean result = filter.isAccessAllowed( mockRequest, null, null );
//        
//        Assert.assertTrue( result );
        
    }
    
}
