package org.sonatype.nexus.jsecurity;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.util.FileUtils;

public class DefaultNexusSecurityUpgradeTest
    extends PlexusTestCase
{

    private static final String ORG_CONFIG_FILE = "target/test-classes/org/sonatype/nexus/jsecurity/security.xml";

    private final String workDir = "target/DefaultNexusSecurityUpgradeTest/work/";
    
    private final String configLocation = workDir+"conf/security.xml";

    
    
    public void testDoUpgrade() throws Exception
    {
        NexusSecurity nexusSecurity = (NexusSecurity) this.lookup( NexusSecurity.class );
        nexusSecurity.startService();
    }
    
    
    
    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        // copy the file to a different location because we are going to change it
        FileUtils.copyFile( new File( ORG_CONFIG_FILE ), new File( configLocation ) );
    }

    @Override
    protected void customizeContext( Context context )
    {
        super.customizeContext( context );

        context.put( "nexus-work", workDir );
        
//        context.put( "security-xml-file", COPY_CONFIG_FILE );
    }

}
