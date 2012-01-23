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
package org.sonatype.nexus.gwt.ui.client.table;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.ClickListenerCollection;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SourcesClickEvents;
import com.google.gwt.user.client.ui.Widget;

public class ToolBarButton extends Composite implements ClickListener, SourcesClickEvents {

    private PushButton button = new PushButton();
    private Label label = new Label();

    private ClickListenerCollection clickListeners;

    public ToolBarButton(String normalURL, String disabledURL, String labelText) {
        this(normalURL, disabledURL, labelText, true);
    }

    public ToolBarButton(String normalURL, String disabledURL, String labelText, boolean showLabel) {
        // Set up the button
        Image normalImage = new Image(normalURL, 0, 0, 16, 16);
        Image disabledImage = new Image(disabledURL, 0, 0, 16, 16);
        button.getUpFace().setImage(normalImage);
        button.getDownFace().setImage(normalImage);
        button.getUpDisabledFace().setImage(disabledImage);
        button.getDownDisabledFace().setImage(disabledImage);
        button.setTitle(labelText);

        // Set up the label
        label.setText(labelText);

        // Hook up events.
        button.addClickListener(this);
        label.addClickListener(this);

        // Create the panel and add the widgets to it
        HorizontalPanel panel = new HorizontalPanel();
        panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        panel.add(button);
        if (showLabel) {
            panel.add(label);
        }
        initWidget(panel);

        // Set the style of the panel
        setStyleName("toolbar-button");
    }

    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
        if (enabled) {
            label.removeStyleDependentName("disabled");
        } else {
            label.addStyleDependentName("disabled");
        }
    }

    public void addClickListener(ClickListener listener) {
        if (clickListeners == null) {
            clickListeners = new ClickListenerCollection();
        }
        clickListeners.add(listener);
    }

    public void removeClickListener(ClickListener listener) {
        if (clickListeners != null) {
            clickListeners.remove(listener);
        }
    }

    public void onClick(Widget sender) {
        if (clickListeners != null) {
            clickListeners.fireClick(this);
        }
    }

}
