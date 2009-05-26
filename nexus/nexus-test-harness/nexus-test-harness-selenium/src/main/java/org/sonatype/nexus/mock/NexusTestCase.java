package org.sonatype.nexus.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.mock.util.PropUtil;

@Ignore
public abstract class NexusTestCase {
    private static MockNexusEnvironment env;

    @BeforeClass
    public synchronized static void startNexus() throws Exception {
        if (env == null) {
            env = new MockNexusEnvironment(PropUtil.get("jettyPort", 12345), "/nexus");
            env.start();

            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    try {
                        env.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }));
        }
    }

    @Before
    public void mockSetup() {
        MockHelper.clearMocks();
    }

    @After
    public void mockCleanup() {
        MockHelper.checkAssertions();
    }
}
