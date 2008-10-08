package org.sonatype.nexus.integrationtests.nexus874;

import java.io.File;
import java.net.ConnectException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.NexusStateUtil;

/**
 * Validate the MemoryRealm that replaces default nexus security
 */
public class Nexus874SecurityRealmReplacementTest
    extends AbstractPrivilegeTest
{    
    public Nexus874SecurityRealmReplacementTest()
    {
    }
    
    @Before
    public void copyFile()
        throws Exception
    {
        File memoryRealmJar = new File( "target/copied-dependencies/nexus-simple-memory-realm.jar" );
        
        File destinationJar = new File( AbstractNexusIntegrationTest.nexusBaseDir + "/runtime/apps/nexus/lib/nexus-simple-memory-realm.jar" );
        
        FileTestingUtils.fileCopy( memoryRealmJar, destinationJar );
    }

    @After
    public void deleteFile()
        throws Exception
    {
        new File( AbstractNexusIntegrationTest.nexusBaseDir + "/runtime/apps/nexus/lib/nexus-simple-memory-realm.jar" ).delete();
    }
    
    @Test
    public void authentication()
        throws Exception
    {   
        if( printKnownErrorButDoNotFail( Nexus874SecurityRealmReplacementTest.class, "authorization" ))
        {
            return;
        }
        
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
