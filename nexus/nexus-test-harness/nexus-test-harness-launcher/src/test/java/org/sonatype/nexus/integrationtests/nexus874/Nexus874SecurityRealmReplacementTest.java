package org.sonatype.nexus.integrationtests.nexus874;

import java.net.ConnectException;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.NexusStateUtil;

/**
 * Validate the MemoryRealm that replaces default nexus security
 */
public class Nexus874SecurityRealmReplacementTest
    extends AbstractNexusIntegrationTest
{    
    public Nexus874SecurityRealmReplacementTest()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }
        
    @Test
    public void authentication()
        throws Exception
    {           
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "admin123" );
        
        NexusStateUtil.getNexusStatus();
        
        TestContainer.getInstance().getTestContext().setUsername( "deployment" );
        TestContainer.getInstance().getTestContext().setPassword( "deployment123" );
        
        NexusStateUtil.getNexusStatus();
        
        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "anonymous" );
        
        NexusStateUtil.getNexusStatus();
        
        TestContainer.getInstance().getTestContext().setUsername( "admin" );
        TestContainer.getInstance().getTestContext().setPassword( "badpassword" );
        
        try
        {
            NexusStateUtil.getNexusStatus();
            Assert.fail();
        }
        catch ( ConnectException e )
        {
            //good
        }
        
        TestContainer.getInstance().getTestContext().setUsername( "deployment" );
        TestContainer.getInstance().getTestContext().setPassword( "badpassword" );
        
        try
        {
            NexusStateUtil.getNexusStatus();
            Assert.fail();
        }
        catch ( ConnectException e )
        {
            //good
        }
        
        TestContainer.getInstance().getTestContext().setUsername( "anonymous" );
        TestContainer.getInstance().getTestContext().setPassword( "badpassword" );
        
        try
        {
            NexusStateUtil.getNexusStatus();
            Assert.fail();
        }
        catch ( ConnectException e )
        {
            //good
        }
    }
    
    public void authorization()
        throws Exception
    {
        if( printKnownErrorButDoNotFail( Nexus874SecurityRealmReplacementTest.class, "authorization" ))
        {
            return;
        }
    }
}
