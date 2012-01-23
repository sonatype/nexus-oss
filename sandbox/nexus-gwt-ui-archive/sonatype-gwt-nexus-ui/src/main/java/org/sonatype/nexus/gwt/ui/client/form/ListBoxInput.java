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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.user.client.ui.ListBox;

/**
 *
 * @author barath
 */
public class ListBoxInput implements FormInput {
    
    private ListBox widget;

    public ListBoxInput(ListBox widget) {
        this.widget = widget;
    }

    public Object getValue() {
        Object value = null;
        if (widget.isMultipleSelect()) {
            List selecteds = new ArrayList();
            for (int i = 0; i < widget.getItemCount(); i++) {
                if (widget.isItemSelected(i)) {
                    selecteds.add(widget.getValue(i));
                }
            }
            value = selecteds;
        } else {
            if (widget.getSelectedIndex() > -1) {
                value = widget.getValue(widget.getSelectedIndex());
            }
        }
        return value;
    }

    public void setValue(Object value) {
        if (value instanceof Collection && widget.isMultipleSelect()) {
            for (Iterator i = ((Collection) value).iterator(); i.hasNext();) {
                for (int j = 0; j < widget.getItemCount(); j++) {
                    if (widget.getValue(j).equals(i.next())) {
                        widget.setItemSelected(j, true);
                    }
                }
            }
        } else {
            for (int i = 0; i < widget.getItemCount(); i++) {
                if (widget.getValue(i).equals(value)) {
                    widget.setItemSelected(i, true);
                    break;
                }
            }
        }
    }

    public void reset() {
        for (int i = 0; i < widget.getItemCount(); i++) {
            widget.setItemSelected(i, false);
        }
        widget.setSelectedIndex(-1);
        if (widget.getVisibleItemCount() <= 1) {
            widget.setSelectedIndex(0);
            widget.setItemSelected(0, true);
        }
    }

}
