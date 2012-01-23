/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
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
