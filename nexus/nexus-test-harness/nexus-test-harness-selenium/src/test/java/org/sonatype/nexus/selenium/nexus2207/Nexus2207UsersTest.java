package org.sonatype.nexus.selenium.nexus2207;

import static org.hamcrest.CoreMatchers.equalTo;

import org.junit.Assert;
import org.junit.Test;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.UsersConfigurationForm;
import org.sonatype.nexus.mock.pages.UsersTab;
import org.sonatype.nexus.selenium.nexus1815.LoginTest;
import org.sonatype.nexus.selenium.util.NxAssert;

public class Nexus2207UsersTest
    extends SeleniumTest
{

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        UsersConfigurationForm users = main.openUsers().addUser();

        NxAssert.requiredField( users.getUserId(), "seluser" );
        NxAssert.requiredField( users.getName(), "seluser" );
        NxAssert.requiredField( users.getEmail(), "seluser@sonatype.org" );
        NxAssert.requiredField( users.getStatus(), "Active" );

        users.save();
        NxAssert.hasErrorText( users.getRoles(), "Select one or more items" );
        users.getRoles().addAll();
        NxAssert.noErrorText( users.getRoles() );

        users.getPassword().type( "asd" );
        users.getPasswordConfirm().type( "dsa" );
        Assert.assertTrue( users.getPasswordConfirm().hasErrorText( "Passwords don't match" ) );
        users.getPasswordConfirm().type( "asd" );
        Assert.assertFalse( users.getPasswordConfirm().hasErrorText( "Passwords don't match" ) );

        users.cancel();
    }

    @Test
    public void userCrud()
        throws InterruptedException
    {
        LoginTest.doLogin( main );

        // create
        UsersTab users = main.openUsers();
        String userId = "seleniumuser";
        String name = "seleniumuser name";
        String email = "seleniumuser@sonatype.org";
        String status = "active";
        String uiRole = "ui-basic";
        users.addUser().populate( userId, name, email, status, "seleniumuserpw", uiRole ).save();
        users.refresh();

        Assert.assertTrue( users.getGrid().contains( userId ) );

        users.refresh();

        // read
        UsersConfigurationForm user = users.select( userId ).selectConfiguration();
        Assert.assertThat( user.getUserId().getValue(), equalTo( userId ) );
        Assert.assertThat( user.getName().getValue(), equalTo( name ) );
        Assert.assertThat( user.getEmail().getValue(), equalTo( email ) );
        Assert.assertThat( user.getStatus().getValue(), equalTo( status ) );
        Assert.assertTrue( user.getRoles().containsLeftSide( uiRole ) );
        Assert.assertFalse( user.getRoles().containsLeftSide( "admin" ) );

        users.refresh();

        // update
        String disable = "disabled";
        String newName = "selenium new user name";

        user = users.select( userId ).selectConfiguration();
        user.getStatus().setValue( disable );
        user.getName().type( newName );
        user.save();

        users.refresh();
        user = users.select( userId ).selectConfiguration();
        Assert.assertThat( user.getName().getValue(), equalTo( newName ) );
        Assert.assertThat( user.getStatus().getValue(), equalTo( disable ) );

        users.refresh();

        users.select( userId );
        users.delete().clickYes();

        users.refresh();

        Assert.assertFalse( users.getGrid().contains( userId ) );
    }

}
