package org.sonatype.nexus.integrationtests.nexus166;

import java.io.File;

import org.sonatype.nexus.test.utils.NexusStatusUtil;
import org.sonatype.nexus.test.utils.TestProperties;
import org.testng.annotations.Test;

public class HardStartBundle
{

    @Test
    public void runForkedBundle()
        throws Exception
    {
        String nexus =
            new File( TestProperties.getFile( "nexus-base-dir" ), "bin/jsw/windows-x86-32/nexus.bat" ).getCanonicalPath();

        Process p = Runtime.getRuntime().exec( nexus );

        while ( !new NexusStatusUtil().isNexusRunning() )
        {
            System.out.println( "Nexus still not started" );
        }

        // FIXME that doesn't kill and the IT never exits
        p.destroy();
    }

}
