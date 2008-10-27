package org.sonatype.nexus.seleniumtests;

import org.sonatype.appbooter.ForkedAppBooter;

public class Nexus
{

    public static void start()
        throws Exception
    {
        ForkedAppBooter appBooter =
            (ForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );
        appBooter.start();
    }

    public static void stop()
        throws Exception
    {
        ForkedAppBooter appBooter =
            (ForkedAppBooter) TestContainer.getInstance().lookup( ForkedAppBooter.ROLE, "TestForkedAppBooter" );
        appBooter.stop();

    }

}
