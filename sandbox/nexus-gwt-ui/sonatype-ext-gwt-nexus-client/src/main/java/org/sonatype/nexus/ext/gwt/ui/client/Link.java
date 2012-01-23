/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
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
