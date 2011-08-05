/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
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

import com.google.gwt.user.client.ui.CheckBox;

/**
 * 
 *
 * @author barath
 */
public class CheckBoxInput implements FormInput {

    private CheckBox widget;
    
    public CheckBoxInput(CheckBox widget) {
        this.widget = widget;
    }
    
    public Object getValue() {
        return Boolean.valueOf(widget.isChecked());
    }

    public void setValue(Object value) {
        if (value != null) {
            boolean b = false;
            if (value instanceof String ) {
                b = Boolean.valueOf((String) value).booleanValue();
            } else if (value instanceof Boolean) {
                b = ((Boolean) value).booleanValue();
            }
            widget.setChecked(b);
        } else {
            widget.setChecked(false);
        }
    }

    public void reset() {
        widget.setChecked(false);
    }

}
