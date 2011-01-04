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

import org.sonatype.nexus.mock.components.Button;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.Window;

import com.thoughtworks.selenium.Selenium;

public class SetPasswordWindow
    extends Window
{
    private Button cancelButton;

    private TextField confirmPassword;

    private TextField newPassword;

    private Button okButton;

    public SetPasswordWindow( Selenium selenium )
    {
        super( selenium, "window.Ext.getCmp('set-password-window')" );

        newPassword = new TextField( this, ".find('name', 'newPassword')[0]" );
        confirmPassword = new TextField( this, ".find('name', 'confirmPassword')[0]" );

        okButton = new Button( selenium, expression + ".items.items[0].buttons[0]" );
        cancelButton = new Button( selenium, expression + ".items.items[0].buttons[1]" );
    }

    public final Button getCancelButton()
    {
        return cancelButton;
    }

    public final TextField getConfirmPassword()
    {
        return confirmPassword;
    }

    public final TextField getNewPassword()
    {
        return newPassword;
    }

    public final Button getOkButton()
    {
        return okButton;
    }

    public MessageBox ok()
    {
        okButton.click();

        return new MessageBox(selenium);
    }

    public SetPasswordWindow populate( String newUserPw )
    {
        newPassword.type( newUserPw );
        confirmPassword.type( newUserPw );

        return this;
    }

    public void cancel()
    {
        cancelButton.click();
    }

}
