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

import com.google.gwt.user.client.ui.TextBox;

/**
 * 
 *
 * @author barath
 */
public class NumberBoxInput implements FormInput {

    private TextBox widget;
    
    private boolean integer;
    
    public NumberBoxInput(TextBox widget) {
        this(widget, true);
    }

    public NumberBoxInput(TextBox widget, boolean isInteger) {
        this.widget = widget;
        this.integer = isInteger;
    }

    public Object getValue() {
        Double n = null;
        if (widget.getText() != null && !"".equals(widget.getText().trim())) {
            n = Double.valueOf(widget.getText());
        }
        return n;
    }
    
    public void setValue(Object value) {
        if (value != null) {
            String s = String.valueOf(value);
            if (integer) {
                int pos = s.indexOf('.');
                if (pos > -1) {
                    s = s.substring(0, pos);
                }
            }
            widget.setText(s);
        } else {
            widget.setText("");
        }
    }

    public void reset() {
        widget.setText("");
    }

}
