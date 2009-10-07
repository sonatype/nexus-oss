package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class Button extends Component {
    public Button(Selenium selenium, String expression) {
        super(selenium, expression);
    }

    public boolean disabled() {
        return evalTrue(".disabled");
    }

    public void click() {
        waitForEvalTrue(".disabled == false");
        selenium.click(getXPath());
    }

    public void clickNoWait() {
        selenium.click(getXPath());
    }
}
