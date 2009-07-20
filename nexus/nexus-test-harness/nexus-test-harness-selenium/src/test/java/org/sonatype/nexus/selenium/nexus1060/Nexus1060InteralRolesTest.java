package org.sonatype.nexus.selenium.nexus1060;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RolesConfigurationForm;
import org.sonatype.nexus.mock.pages.RolesTab;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;

public class Nexus1060InteralRolesTest
    extends SeleniumTest
{
    @Test
    public void internalRoles()
        throws InterruptedException
    {
        LoginTest.doLogin( main );
        
        RolesTab roles = main.openRoles();
        
        RolesConfigurationForm role = roles.select( "anonymous" ).selectConfiguration();
        
        Assert.assertTrue( role.getRoleId().isDisabled() );
        Assert.assertTrue( role.getName().isDisabled() );
        Assert.assertTrue( role.getDescription().isDisabled() );
        Assert.assertTrue( role.getSessionTimeout().isDisabled() );
    }
}
