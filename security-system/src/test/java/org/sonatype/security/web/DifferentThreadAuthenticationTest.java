package org.sonatype.security.web;

import junit.framework.Assert;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.jsecurity.authc.UsernamePasswordToken;
import org.jsecurity.subject.SimplePrincipalCollection;
import org.jsecurity.subject.Subject;
import org.jsecurity.web.WebUtils;
import org.sonatype.security.AbstractSecurityTest;
import org.sonatype.security.SecuritySystem;

public class DifferentThreadAuthenticationTest
extends AbstractSecurityTest
{

    public void testGetSubjectFromThread() throws Exception
    {
        SecuritySystem securitySystem = this.lookup( SecuritySystem.class );
        securitySystem.start();
        
        // need to bind to a request
        this.setupLoginContext( "testGetSubjectFromThread" );
        
        Assert.assertNotNull( securitySystem.login( new UsernamePasswordToken("jcoder", "jcoder") ) );
        
        WebUtils.unbindServletRequest();
        WebUtils.unbindServletResponse();
        
        // now with the thread
        SubjectRetrievingThread thread = new SubjectRetrievingThread( this.getContainer() );
        
        thread.setContextClassLoader( null );
        thread.start();
        thread.join( 500 );
        Assert.assertNotNull( thread.getSubject() );
        Subject subject = thread.getSubject();
        Assert.assertTrue( subject.hasRole( "RoleA" ) );
        
        // if we login again with the jcoder user we should need to bind the request again
        try
        {
            securitySystem.login( new UsernamePasswordToken("jcoder", "jcoder") );
            Assert.fail( "Expected IllegalStateException" );
        }
        catch( IllegalStateException e)
        {
            // this is not a great exception to catch...
            // but we check the success on the next call
        }
        
        this.setupLoginContext( "testGetSubjectFromThread-again" );
        subject = securitySystem.login( new UsernamePasswordToken("jcoder", "jcoder") );
        Assert.assertNotNull( subject );
        
    }
    
    class SubjectRetrievingThread extends Thread
    {   
        private PlexusContainer container;
//        private Subject subject;
        private SecuritySystem securitySystem;
        
        
        private SubjectRetrievingThread( PlexusContainer container ) throws ComponentLookupException
        {
            this.container = container;
            this.securitySystem = container.lookup( SecuritySystem.class );
        }

        @Override
        public void run()
        {
            this.securitySystem.runAs( new SimplePrincipalCollection("jcoder", "") );
        }
        
        public Subject getSubject()
        {
            return this.securitySystem.getSubject();
        }
    }
}
