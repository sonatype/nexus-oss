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
package org.sonatype.nexus.mock.pages;

import java.util.concurrent.TimeUnit;

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.Window;
import org.sonatype.nexus.mock.models.User;
import org.sonatype.nexus.mock.util.ThreadUtils;

import com.thoughtworks.selenium.Selenium;

public class LoginWindow
    extends Window
{
    private TextField username;

    private TextField password;

    private Button loginButton;

    private MainPage mainPage;

    public LoginWindow( Selenium selenium, MainPage mainPage )
    {
        super( selenium, "window.Ext.getCmp('login-window')" );
        this.mainPage = mainPage;

        username = new TextField( selenium, "window.Ext.getCmp('usernamefield')" );
        password = new TextField( selenium, "window.Ext.getCmp('passwordfield')" );
        loginButton = new Button( selenium, "window.Ext.getCmp('loginbutton')" );
    }

    public LoginWindow populate( User user )
    {
        this.username.type( user.getUsername() );
        this.password.type( user.getPassword() );

        return this;
    }

    public LoginWindow populate( String username, String password )
    {
        this.username.type( username );
        this.password.type( password );

        return this;
    }

    public LoginWindow login()
    {
        loginButton.click();

        return this;
    }

    public TextField getUsername()
    {
        return username;
    }

    public TextField getPassword()
    {
        return password;
    }

    public Button getLoginButton()
    {
        return loginButton;
    }

    public void loginExpectingSuccess()
    {
        login();
        waitForHidden();

        // wait for the login-link to change
        ThreadUtils.waitFor( new ThreadUtils.WaitCondition()
        {
            public boolean checkCondition( long elapsedTimeInMs )
            {
                return !mainPage.loginLinkAvailable();
            }
        }, TimeUnit.SECONDS, 15 );
    }
}
