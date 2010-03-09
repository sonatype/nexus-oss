package org.sonatype.nexus.selenium.nexus1060;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RolesConfigurationForm;
import org.sonatype.nexus.mock.pages.RolesTab;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus1060InteralRolesTest.class )
public class Nexus1060InteralRolesTest
    extends SeleniumTest
{
    @Test
    public void internalRoles()
        throws InterruptedException
    {
        doLogin();

        RolesTab roles = main.openRoles();

        RolesConfigurationForm role = roles.select( "anonymous" ).selectConfiguration();

        Assert.assertTrue( role.getRoleId().isDisabled() );
        Assert.assertTrue( role.getName().isDisabled() );
        Assert.assertTrue( role.getDescription().isDisabled() );
    }
}
