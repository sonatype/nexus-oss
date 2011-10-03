/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/oss/attributions
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
