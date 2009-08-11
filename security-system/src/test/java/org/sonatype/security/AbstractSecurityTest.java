package org.sonatype.security;

import static org.easymock.EasyMock.replay;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;
import org.easymock.EasyMock;
import org.jsecurity.web.WebUtils;

public abstract class AbstractSecurityTest
    extends PlexusTestCase
{

    protected File PLEXUS_HOME = new File( "./target/plexus-home/" );

    protected File APP_CONF = new File( PLEXUS_HOME, "conf" );

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );
        context.put( "application-conf", APP_CONF.getAbsolutePath() );
    }

    @Override
    protected void setUp()
        throws Exception
    {
        // delete the plexus home dir
        FileUtils.deleteDirectory( PLEXUS_HOME );

        this.getSecuritySystem().start();
        
        super.setUp();
    }

    protected SecuritySystem getSecuritySystem()
        throws Exception
    {
        return this.lookup( SecuritySystem.class );
    }

    protected void setupLoginContext( String sessionId )
    {
        HttpServletRequest mockRequest = EasyMock.createNiceMock( HttpServletRequest.class );
        HttpServletResponse mockResponse = EasyMock.createNiceMock( HttpServletResponse.class );
        HttpSession mockSession = EasyMock.createNiceMock( HttpSession.class );

        EasyMock.expect( mockSession.getId() ).andReturn( sessionId ).anyTimes();
        EasyMock.expect( mockRequest.getCookies() ).andReturn( null ).anyTimes();
        EasyMock.expect( mockRequest.getSession() ).andReturn( mockSession ).anyTimes();
        EasyMock.expect( mockRequest.getSession( false ) ).andReturn( mockSession ).anyTimes();
        replay( mockSession );
        replay( mockRequest );

        // we need to bind for the "web" impl of the RealmSecurityManager to work
        WebUtils.bind( mockRequest );
        WebUtils.bind( mockResponse );
    }

}
