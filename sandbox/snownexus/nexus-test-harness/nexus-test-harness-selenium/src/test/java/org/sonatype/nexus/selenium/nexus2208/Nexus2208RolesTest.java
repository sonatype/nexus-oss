package org.sonatype.nexus.selenium.nexus2208;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.RolesConfigurationForm;
import org.sonatype.nexus.mock.pages.RolesTab;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;
import org.sonatype.nexus.selenium.util.NxAssert;
import org.testng.Assert;
import org.testng.annotations.Test;

@Component( role = Nexus2208RolesTest.class )
public class Nexus2208RolesTest
    extends SeleniumTest
{

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        RolesConfigurationForm roles = main.openRoles().addRole();

        NxAssert.requiredField( roles.getRoleId(), "selrole" );
        NxAssert.requiredField( roles.getName(), "selrole" );
        NxAssert.requiredField( roles.getSessionTimeout(), "60" );

        roles.save();
        NxAssert.hasErrorText( roles.getPrivileges(), "One or more roles or privileges are required" );
        roles.getPrivileges().addAll();
        NxAssert.noErrorText( roles.getPrivileges() );

        roles.cancel();
    }

    @Test
    public void roleCRUD()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        RolesTab roles = main.openRoles();

        // create
        String roleId = "selrole";
        String name = "selrolename";
        String timeout = "60";
        String priv = "admin";
        roles.addRole().populate( roleId, name, timeout, priv ).save();
        roles.refresh();

        Assert.assertTrue( roles.getGrid().contains( roleId ) );
        roles.refresh();

        // read
        RolesConfigurationForm role = roles.select( roleId ).selectConfiguration();
        NxAssert.valueEqualsTo( role.getRoleId(), roleId );
        NxAssert.valueEqualsTo( role.getName(), name );
        NxAssert.valueEqualsTo( role.getSessionTimeout(), timeout );
        NxAssert.contains( role.getPrivileges(), priv );

        roles.refresh();

        // update
        String newTO = "30";
        String newName = "new selenium role name";

        role = roles.select( roleId ).selectConfiguration();
        role.getName().type( newName );
        role.getSessionTimeout().type( newTO );
        role.save();

        roles.refresh();
        role = roles.select( roleId ).selectConfiguration();
        NxAssert.valueEqualsTo( role.getName(), newName );
        NxAssert.valueEqualsTo( role.getSessionTimeout(), newTO );

        roles.refresh();

        //delete
        roles.select( roleId );
        roles.delete().clickYes();
        roles.refresh();

        Assert.assertFalse( roles.getGrid().contains( roleId ) );
    }
}
