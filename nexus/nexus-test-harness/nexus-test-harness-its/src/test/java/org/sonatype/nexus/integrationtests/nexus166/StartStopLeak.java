package org.sonatype.nexus.integrationtests.nexus166;

import javax.swing.JOptionPane;

import org.junit.After;
import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

public class StartStopLeak
    extends AbstractNexusIntegrationTest
{
    @After
    public void stop()
        throws Exception
    {
        // JOptionPane.showConfirmDialog( null, "Stop" );
        getNexusStatusUtil().stop();
        System.gc();
        JOptionPane.showConfirmDialog( null, "Stoped" );
    }

    @Test
    public void test()
    {
        System.out.println( "In use: " + ( Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() )
            / 1024 / 1024 );
    }
}
