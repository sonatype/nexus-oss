package org.sonatype.nexus.selenium.nexus2209;

import org.junit.Test;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.PrivilegeConfigurationForm;
import org.sonatype.nexus.mock.pages.PrivilegesTab;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;
import org.sonatype.nexus.selenium.util.NxAssert;

public class Nexus2209PrivilegesTest
    extends SeleniumTest
{

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        PrivilegeConfigurationForm privs = main.openPrivileges().addPrivilege();

        NxAssert.requiredField( privs.getName(), "selpriv" );
        NxAssert.requiredField( privs.getDescription(), "selpriv" );

        privs.save();
        NxAssert.hasErrorText( privs.getRepoTarget(), "Repository Target is required." );

        privs.getRepoTarget().select( 0 );
        NxAssert.noErrorText( privs.getRepoTarget() );

        privs.cancel();
    }

    @Test
    public void privsCrud()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        PrivilegesTab privs = main.openPrivileges();

        // create
        String name = "privName";
        String description = "privDescription";
        privs.addPrivilege().populate( name, description, 0 ).save();
        privs.refresh();
/*
        Assert.assertTrue( privs.getGrid().contains( name ) );
        privs.refresh();

        // read
        PrivilegeConfigurationForm priv = privs.select( name ).selectConfiguration();
        NxAssert.valueEqualsTo( priv.getName(), name );
        NxAssert.valueEqualsTo( priv.getDescription(), description );
        NxAssert.valueEqualsTo( priv.getRepoTarget(), String.valueOf( 0 ) );

        privs.refresh();*/

    }
}
