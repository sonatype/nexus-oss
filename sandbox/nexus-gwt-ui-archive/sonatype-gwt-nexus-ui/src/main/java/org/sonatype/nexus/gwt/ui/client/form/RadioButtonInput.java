/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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

import com.google.gwt.user.client.ui.RadioButton;

/**
 * 
 *
 * @author barath
 */
public class RadioButtonInput implements FormInput {
    
    private RadioButton[] widgets;
    
    public RadioButtonInput(RadioButton[] widgets) {
        this.widgets = widgets;
    }

    public Object getValue() {
        for (int i = 0; i < widgets.length; i++) {
            if (widgets[i].isChecked()) {
                return widgets[i].getName();
            }
        }
        return null;
    }

    public void setValue(Object value) {
        String name = null;
        if (value != null) {
            name = value.toString();
        }
        for (int i = 0; i < widgets.length; i++) {
            widgets[i].setChecked(widgets[i].getName().equals(name));
        }
    }

    public void reset() {
        widgets[0].setChecked(true);
        for (int i = 1; i < widgets.length; i++) {
            widgets[i].setChecked(false);
        }
    }

}
