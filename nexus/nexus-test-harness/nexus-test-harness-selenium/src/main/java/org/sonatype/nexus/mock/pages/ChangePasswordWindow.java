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
