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
package org.sonatype.nexus.gwt.ui.client.form;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 *
 * @author barath
 */
public class RadioButtonGroup extends Composite implements ClickListener {
    
    private RadioButton[] buttons;
    
    public RadioButtonGroup(RadioButton[] buttons) {
        this(buttons, new FlowPanel());
    }

    public RadioButtonGroup(RadioButton[] buttons, HasWidgets container) {
        this.buttons = buttons;
        
        for (int i = 0; i < buttons.length; i++) {
            container.add(buttons[i]);
            buttons[i].addClickListener(this);
        }
        buttons[0].setChecked(true);
        initWidget((Widget) container);
    }

    public void onClick(Widget sender) {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i] != sender) {
                buttons[i].setChecked(false);
            }
        }
    }

}
