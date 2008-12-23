package org.sonatype.nexus.integrationtests.nexus1071;

import org.junit.After;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.integrationtests.TestContainer;

public class AbstractAnonAccessTest
    extends AbstractMavenNexusIT
{
    @Override
    public void oncePerClassSetUp()
        throws Exception
    {
        // this starts nexus with security enabled
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        super.oncePerClassSetUp();

        // this tells test harness NOT to login into nexus 
        TestContainer.getInstance().getTestContext().setSecureTest( false );
    }

    @After
    public void reenableSecurity()
    {
        // IT won't be able to shutdown nexus if security is disabled
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

}
