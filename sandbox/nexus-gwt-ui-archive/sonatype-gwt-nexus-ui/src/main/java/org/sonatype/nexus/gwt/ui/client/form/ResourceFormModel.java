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

import java.util.Iterator;
import java.util.Map;

import org.sonatype.nexus.gwt.ui.client.data.ResourceParser;

import com.google.gwt.user.client.Window;

/**
 *
 * @author barath
 */
public class ResourceFormModel extends AbstractFormModel {
    
    private ResourceParser parser;

    private Object resource;
    
    public ResourceFormModel(ResourceParser parser) {
        this.parser = parser;
    }

    public Object getFormData() {
        String name = null;
        try {
            for (Iterator i = inputs.entrySet().iterator(); i.hasNext();) {
                Map.Entry entry = (Map.Entry) i.next();
                name = (String) entry.getKey();
                FormInput input = (FormInput) entry.getValue();
                parser.setValue(resource, name, input.getValue());
            }
            return resource;
        } catch (Exception e) {
            e.printStackTrace();
            Window.alert("Wrong value in '" + name + "' field!");
        }
        return null;
    }

    public void setFormData(Object formData) {
        this.resource = formData;
        for (Iterator i = inputs.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            FormInput input = (FormInput) entry.getValue();
            Object obj = parser.getValue(resource, (String) entry.getKey());
            input.setValue(obj);
        }
    }

}
