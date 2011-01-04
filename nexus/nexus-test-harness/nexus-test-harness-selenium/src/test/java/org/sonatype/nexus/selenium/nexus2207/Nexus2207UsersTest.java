/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.selenium.nexus2207;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.hamcrest.CoreMatchers;
import org.restlet.data.Status;
import org.sonatype.nexus.mock.MockResponse;
import org.sonatype.nexus.mock.SeleniumTest;
import org.sonatype.nexus.mock.pages.MessageBox;
import org.sonatype.nexus.mock.pages.SetPasswordWindow;
import org.sonatype.nexus.mock.pages.UsersConfigurationForm;
import org.sonatype.nexus.mock.pages.UsersTab;
import org.sonatype.nexus.mock.rest.MockHelper;
import org.sonatype.nexus.selenium.util.NxAssert;
import org.sonatype.security.SecuritySystem;
import org.sonatype.security.rest.model.UserChangePasswordRequest;
import org.sonatype.security.rest.model.UserChangePasswordResource;
import org.sonatype.security.usermanagement.DefaultUser;
import org.sonatype.security.usermanagement.RoleIdentifier;
import org.sonatype.security.usermanagement.User;
import org.sonatype.security.usermanagement.UserNotFoundException;
import org.sonatype.security.usermanagement.UserStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Component( role = Nexus2207UsersTest.class )
public class Nexus2207UsersTest
    extends SeleniumTest
{

    private static final String ORIGINAL_PW = "developer";

    @Requirement
    private SecuritySystem securitySystem;

    private User user;

    @BeforeClass
    public void createUser()
        throws Exception
    {
        user = new DefaultUser();
        user.setUserId( "developer" );
        user.setFirstName( "Developer" );
        user.setLastName( "Developer" );
        user.setEmailAddress( "email@sonatype.org" );
        user.setRoles( Collections.singleton( new RoleIdentifier( "ui-basic", "ui-basic" ) ) );
        user.setStatus( UserStatus.active );
        user.setSource( "default" );
        securitySystem.addUser( user, ORIGINAL_PW );
    }

    @AfterClass
    public void deleteUser()
        throws UserNotFoundException
    {
        securitySystem.deleteUser( user.getUserId() );
    }

    @Test
    public void errorMessages()
        throws InterruptedException
    {
        doLogin();

        UsersTab users = main.openUsers();

        UsersConfigurationForm user = users.addUser();

        NxAssert.requiredField( user.getUserId(), "seluser" );

        user.getFirstName().type( " space" );
        Assert.assertTrue( user.getFirstName().hasErrorText( "First Name cannot start with whitespace." ) );
        user.getFirstName().resetValue();
        user.getFirstName().type( "seluser" );
        
        //FIXME
//        NxAssert.requiredField( user.getFirstName(), "seluser" );
//        NxAssert.requiredField( user.getLastName(), "seluser" );
        NxAssert.requiredField( user.getEmail(), "seluser@sonatype.org" );
        NxAssert.requiredField( user.getStatus(), "Active" );

        user.save();
        NxAssert.hasErrorText( user.getRoles(), "Select one or more items" );
        user.getRoles().addAll();
        NxAssert.noErrorText( user.getRoles() );

        user.getPassword().type( "asd" );
        user.getPasswordConfirm().type( "dsa" );
        Assert.assertTrue( user.getPasswordConfirm().hasErrorText( "Passwords don't match" ) );
        user.getPasswordConfirm().type( "asd" );
        Assert.assertFalse( user.getPasswordConfirm().hasErrorText( "Passwords don't match" ) );

        user.cancel();

        SetPasswordWindow setPw = users.contextMenuSetPassword( "developer" );
        NxAssert.requiredField( setPw.getNewPassword(), "newpw" );
        NxAssert.requiredField( setPw.getConfirmPassword(), "newpw" );

        setPw.getNewPassword().type( "asd" );
        setPw.getConfirmPassword().type( "dsa" );
        NxAssert.hasErrorText( setPw.getConfirmPassword(), "Passwords don't match" );
        setPw.getConfirmPassword().type( "asd" );
        NxAssert.noErrorText( setPw.getConfirmPassword() );

        Assert.assertFalse( setPw.getOkButton().disabled() );
        setPw.cancel();
    }

    @Test
    public void userCrud()
        throws InterruptedException
    {
        doLogin();

        // create
        UsersTab users = main.openUsers();
        String userId = "seleniumuser";
        String name = "seleniumuser name";
        String email = "seleniumuser@sonatype.org";
        String status = "active";
        String uiRole = "ui-basic";
        users.addUser().populate( userId, name, name, email, status, "seleniumuserpw", uiRole ).save();
        users.refresh();

        Assert.assertTrue( users.getGrid().contains( userId ) );

        users.refresh();

        // read
        UsersConfigurationForm user = users.select( userId ).selectConfiguration();
        assertThat( user.getUserId().getValue(), equalTo( userId ) );
        assertThat( user.getFirstName().getValue(), equalTo( name ) );
        assertThat( user.getLastName().getValue(), equalTo( name ) );
        assertThat( user.getEmail().getValue(), equalTo( email ) );
        assertThat( user.getStatus().getValue(), equalTo( status ) );
        Assert.assertTrue( user.getRoles().containsLeftSide( uiRole ) );
        Assert.assertFalse( user.getRoles().containsLeftSide( "admin" ) );

        users.refresh();

        // update
        String disable = "disabled";
        String newName = "selenium new user name";

        user = users.select( userId ).selectConfiguration();
        user.getStatus().setValue( disable );
        user.getFirstName().type( newName );
        user.save();

        users.refresh();
        user = users.select( userId ).selectConfiguration();
        assertThat( user.getFirstName().getValue(), equalTo( newName ) );
        assertThat( user.getStatus().getValue(), equalTo( disable ) );

        users.refresh();

        users.select( userId );
        users.delete().clickYes();

        users.refresh();

        Assert.assertFalse( users.getGrid().contains( userId ) );
    }

    @Test
    public void contextMenu()
        throws InterruptedException
    {
        doLogin();

        UsersTab users = main.openUsers();
        final String userId = "developer";
        MessageBox response;

        // reset password
        MockHelper.expect( "/users_reset/{userId}", new MockResponse( Status.SUCCESS_OK, null ) );
        users.contextMenuResetPassword( userId ).clickYes();
        response = new MessageBox( selenium );
        assertThat( response.getTitle(), CoreMatchers.equalTo( "Password Reset" ) );
        response.clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        MockHelper.expect( "/users_reset/{userId}", new MockResponse( Status.SERVER_ERROR_VERSION_NOT_SUPPORTED, null ) );
        users.contextMenuResetPassword( userId ).clickYes();
        response = new MessageBox( selenium );
        assertThat( response.getTitle(), CoreMatchers.equalTo( "Error" ) );
        response.clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();

        // set password
        final String newUserPw = "newUserPw";
        MockHelper.expect( "/users_setpw", new MockResponse( Status.SUCCESS_OK, null )
        {
            @Override
            public void setPayload( Object payload )
                throws AssertionError
            {
                assertThat( payload, CoreMatchers.notNullValue() );
                UserChangePasswordResource changePw = ( (UserChangePasswordRequest) payload ).getData();
                assertThat( changePw.getUserId(), CoreMatchers.equalTo( userId ) );
                assertThat( changePw.getNewPassword(), CoreMatchers.equalTo( newUserPw ) );
            }
        } );
        response = users.contextMenuSetPassword( userId ).populate( newUserPw ).ok();
        assertThat( response.getTitle(), CoreMatchers.equalTo( "Password Changed" ) );
        response.clickOk();

        MockHelper.checkExecutions();
        MockHelper.checkAssertions();
        MockHelper.clearMocks();

        MockHelper.expect( "/users_setpw", new MockResponse( Status.SERVER_ERROR_VERSION_NOT_SUPPORTED, null ) );
        response = users.contextMenuSetPassword( userId ).populate( "error" ).ok();
        assertThat( response.getTitle(), CoreMatchers.equalTo( "Error" ) );
        response.clickOk();

        MockHelper.checkExecutions();
        MockHelper.clearMocks();
    }

}
