package org.sonatype.nexus.integrationtests.nexus3929;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.UserCreationUtil;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class Nexus3929TimelineCorruptionIT
    extends AbstractNexusIntegrationTest
{

    @Override
    @BeforeMethod( alwaysRun = true )
    public void oncePerClassSetUp()
        throws Exception
    {
        synchronized ( AbstractNexusIntegrationTest.class )
        {
            if ( NEEDS_INIT )
            {
                super.oncePerClassSetUp();

                stopNexus();

                File tl = new File( nexusWorkDir, "timeline/index" );
                while ( FileUtils.listFiles( tl, new String[] { "cfs" }, false ).size() < 7 )
                {
                    startNexus();
                    stopNexus();
                }

                @SuppressWarnings( "unchecked" )
                List<File> cfs = new ArrayList<File>( FileUtils.listFiles( tl, new String[] { "cfs" }, false ) );
                FileUtils.forceDelete( cfs.get( 0 ) );
                FileUtils.forceDelete( cfs.get( 2 ) );
                FileUtils.forceDelete( cfs.get( 5 ) );

                startNexus();
            }
        }
    }

    @Test
    public void login()
        throws Exception
    {
        Status s = UserCreationUtil.login();
        assertTrue( s.isSuccess() );
    }

    @Test
    public void status()
        throws Exception
    {
        assertEquals( getNexusStatusUtil().getNexusStatus().getData().getState(), "STARTED" );
    }
}
