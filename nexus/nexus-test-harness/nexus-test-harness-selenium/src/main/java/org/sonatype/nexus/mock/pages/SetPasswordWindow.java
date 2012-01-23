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
