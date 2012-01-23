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
