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

import org.sonatype.nexus.mock.components.Window;
import org.sonatype.nexus.mock.components.TextField;
import org.sonatype.nexus.mock.components.Button;
import com.thoughtworks.selenium.Selenium;

public class ChangePasswordWindow extends Window {
    private TextField currentPassword;
    private TextField newPassword;
    private TextField confirmPassword;
    private Button button;

    public ChangePasswordWindow(Selenium selenium) {
        super(selenium, "window.Ext.getCmp('change-password-window')");

        currentPassword = new TextField(this, ".findBy(function(c) { return c.fieldLabel == 'Current Password' })[0]");
        newPassword = new TextField(this, ".findBy(function(c) { return c.fieldLabel == 'New Password' })[0]");
        confirmPassword = new TextField(this, ".findBy(function(c) { return c.fieldLabel == 'Confirm Password' })[0]");
        button = new Button(selenium, "window.Ext.getCmp('change-password-button')");
    }

    public ChangePasswordWindow populate(String current, String newPass, String confirm) {
        currentPassword.type(current);
        newPassword.type(newPass);
        confirmPassword.type(confirm);

        return this;
    }

    public PasswordChangedWindow changePasswordExpectingSuccess() {
        button.click();
        waitForHidden();

        return new PasswordChangedWindow(selenium);
    }
}
