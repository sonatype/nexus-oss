package org.sonatype.nexus.mock;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.mock.util.PropUtil;

@Ignore
public abstract class NexusTestCase
{
    private static MockNexusEnvironment env;

    @BeforeClass
    public synchronized static void startNexus()
        throws Exception
    {
        if ( env == null )
        {
            File webappRoot;
            String pathname = PropUtil.get( "webappRoot", null );
            if ( pathname != null )
            {
                webappRoot = new File( pathname ).getAbsoluteFile();
            }
            else
            {
                webappRoot = new File( "../nexus-webapp/src/main/webapp" );
                if ( !webappRoot.exists() )
                {
                    webappRoot = new File( "target/nexus-ui" );
                }
            }

            env = new MockNexusEnvironment( PropUtil.get( "jettyPort", 12345 ), "/nexus", webappRoot );
            env.start();

            Runtime.getRuntime().addShutdownHook( new Thread( new Runnable()
            {
                public void run()
                {
                    try
                    {
                        env.stop();
                    }
                    catch ( Exception e )
                    {
                        e.printStackTrace();
                    }
                }
            } ) );
        }
    }

    @Before
    public void mockSetup()
    {
        MockHelper.clearMocks();
    }

    @After
    public void mockCleanup()
    {
        MockHelper.checkAssertions();
    }
}
