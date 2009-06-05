package org.sonatype.nexus.mock.pages;

import com.thoughtworks.selenium.Selenium;
import org.sonatype.nexus.mock.components.Window;

public class PasswordChangedWindow extends Window {
    public PasswordChangedWindow(Selenium selenium) {
        super(selenium, "window.Ext.getCmp('password-changed-messagebox')");
    }

    public void clickOk() {
        selenium.click("OK");
        waitForHidden();
    }
}
