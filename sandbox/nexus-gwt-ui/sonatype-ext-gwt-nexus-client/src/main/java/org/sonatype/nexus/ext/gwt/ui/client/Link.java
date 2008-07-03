package org.sonatype.nexus.ext.gwt.ui.client;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.widget.Html;
import com.google.gwt.user.client.Element;

public class Link extends Html {
    
    public Link() {
        setTagName("a");
        addStyleName("st-link");
    }

    public Link(String html) {
        this();
        setHtml(html);
    }
    
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        el().addEventsSunk(Events.OnClick);
    }

    public void onComponentEvent(ComponentEvent event) {
        if (event.type == Events.OnClick) {
            event.stopEvent();
            onClick(event);
        }
    }

    public void onClick(ComponentEvent event) {
    }

}
