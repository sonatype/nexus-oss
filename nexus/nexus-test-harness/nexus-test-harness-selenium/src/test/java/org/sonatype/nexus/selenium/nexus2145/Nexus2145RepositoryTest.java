package org.sonatype.nexus.selenium.nexus2145;

import static org.testng.AssertJUnit.assertTrue;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RepositoriesConfigurationForm;
import org.testng.annotations.Test;

@Component( role = Nexus2145RepositoryTest.class )
public class Nexus2145RepositoryTest
    extends SeleniumTest
{

    @Test
    public void errorMessagesHosted()
        throws InterruptedException
    {
        doLogin();

        RepositoriesConfigurationForm newHosted = main.openRepositories().addHostedRepo().save();

        assertTrue( "Task type is a required field", newHosted.getIdField().hasErrorText( "This field is required" ) );
        assertTrue( "Name is a required field", newHosted.getName().hasErrorText( "This field is required" ) );
    }

}
