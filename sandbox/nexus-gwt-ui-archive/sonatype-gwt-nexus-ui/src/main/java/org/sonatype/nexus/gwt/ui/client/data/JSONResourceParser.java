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
package org.sonatype.nexus.gwt.ui.client.data;

import org.sonatype.nexus.gwt.ui.client.JSONUtil;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 *
 * @author barath
 */
public class JSONResourceParser implements ResourceParser {

    public Object getValue(Object obj, String path) {
        return toObject(JSONUtil.getValue((JSONObject) obj, path));
    }

    public void setValue(Object obj, String path, Object value) {
        JSONUtil.setValue((JSONObject) obj, path, parseObject(value));
    }

    private JSONValue parseObject(Object value) {
        JSONValue json = null;
        if (value instanceof String) {
            json = new JSONString((String) value);
        }
        else if (value instanceof Boolean) {
            json = JSONBoolean.getInstance(((Boolean) value).booleanValue());
        }
        else if (value instanceof Number) {
            json = new JSONNumber(((Number) value).doubleValue());
        }
        return json == null ? JSONNull.getInstance() : json;
    }
    
    private Object toObject(JSONValue json) {
        Object obj = null;
        
        if (json instanceof JSONBoolean) {
            obj = Boolean.valueOf(json.isBoolean().booleanValue());
        }
        else if (json instanceof JSONString) {
            obj = json.isString().stringValue();
        }
        else if (json instanceof JSONNumber) {
            obj = new Double(json.isNumber().doubleValue());
        }
        
        return obj;
    }

}
