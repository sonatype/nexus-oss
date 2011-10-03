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
