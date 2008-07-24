package org.sonatype.nexus.gwt.ui.client.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

public class Header extends Widget implements HasText {

    public Header() {
        this(1);
    }

    public Header(int level) {
        setElement(DOM.createElement("h" + level));
    }

    public Header(String text) {
        this(1, text);
    }

    public Header(int level, String text) {
        this(level);
        setText(text);
    }

    public void setText(String text) {
        DOM.setInnerText(getElement(), text);
    }

    public String getText() {
        return DOM.getInnerText(getElement());
    }

}
