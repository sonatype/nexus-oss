package org.sonatype.nexus.gwt.ui.client.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Widget;

public class Span extends Widget implements HasText {

    public Span() {
        setElement(DOM.createSpan());
    }

    public Span(String text) {
        this();
        setText(text);
    }

    public void setText(String text) {
        DOM.setInnerText(getElement(), text);
    }

    public String getText() {
        return DOM.getInnerText(getElement());
    }

}
