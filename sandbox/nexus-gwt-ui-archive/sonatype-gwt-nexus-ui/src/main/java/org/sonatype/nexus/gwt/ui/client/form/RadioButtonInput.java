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
