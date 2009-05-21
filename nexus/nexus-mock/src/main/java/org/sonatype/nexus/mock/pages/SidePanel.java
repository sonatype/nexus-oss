package org.sonatype.nexus.mock.pages;

import org.sonatype.nexus.mock.components.Panel;
import com.thoughtworks.selenium.Selenium;

public abstract class SidePanel extends Panel {
    public SidePanel(Selenium selenium, String expression) {
        super(selenium, expression);
    }

    protected boolean isLinkAvailable(String linkText) {
        String xpath = getXPath() + "//a[text() = '" + linkText + "']";

        return selenium.isElementPresent(xpath) && selenium.isVisible(xpath);
    }

    protected void clickLink(String linkText) {
        String xpath = getXPath() + "//a[text() = '" + linkText + "']";

        selenium.clickAt(xpath, "");
    }

}
