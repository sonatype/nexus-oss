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
