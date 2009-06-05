package org.sonatype.nexus.mock.components;

import com.thoughtworks.selenium.Selenium;

public class Panel extends Component {
    public Panel(Selenium selenium, String expression) {
        super(selenium, expression);
    }

    public void collapse() {
        evalTrue(".collapse()");
        waitForEvalTrue(".collapsed == true");
    }

    public void expand() {
        evalTrue(".expand()");
        waitForEvalTrue(".collapsed == false");
    }
}
