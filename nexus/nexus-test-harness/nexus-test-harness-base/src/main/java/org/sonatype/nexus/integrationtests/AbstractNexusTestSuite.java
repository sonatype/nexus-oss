package org.sonatype.nexus.integrationtests;

import java.io.File;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.bridge.SLF4JBridgeHandler;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.NexusStateUtil;

public abstract class AbstractNexusTestSuite
{

    @BeforeClass
    public static void beforeSuite()
        throws Exception
    {
        
        // configure the logging
        SLF4JBridgeHandler.install();
        
        // copy default nexus.xml
        File testConfigFile = AbstractNexusIntegrationTest.getResource( "default-config/nexus.xml" );
        File outputFile =
            new File( AbstractNexusIntegrationTest.nexusBaseDir + "/"
                + AbstractNexusIntegrationTest.RELATIVE_WORK_CONF_DIR, "nexus.xml" );
        FileTestingUtils.fileCopy( testConfigFile, outputFile );

       NexusStateUtil.doHardStart();

        NexusStateUtil.doSoftStop();

        // enable security
        TestContainer.getInstance().getTestContext().setSecureTest( true );        
    }

    @AfterClass
    public static void afterSuite()
        throws Exception
    {
        NexusStateUtil.doHardStop( false );
    }
    
}
