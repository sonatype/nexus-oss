package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class Window extends Component {
    public Window(Selenium selenium, String expression) {
        super(selenium, expression);
    }

    public void close() {
        selenium.click(getXPath() + "//div[contains(@class, 'x-tool-close')]");
    }
}
