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
package org.sonatype.nexus.gwt.ui.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

/**
 * Utility methods to work with JSON objects.
 *
 * @author barath
 */
public class JSONUtil {
    
    private JSONUtil() {
    }
    
    public static List jsonArrayToList(JSONArray jsonArray) {
        List list = new ArrayList();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                list.add(jsonArray.get(i));
            }
        }
        return list;
    }

    public static JSONValue getValue(JSONObject obj, String path) {
        String[] propChain = path.split("\\.");
        obj = lookupObject(obj, propChain, false);
        return obj == null ? null : obj.get(propChain[propChain.length - 1]);
    }
    
    public static void setValue(JSONObject obj, String path, JSONValue value) {
        setValue(obj, path, value, true);
    }
    
    public static void setValue(JSONObject obj, String path, JSONValue value, boolean createAllowed) {
        String[] propChain = path.split("\\.");
        obj = lookupObject(obj, propChain, createAllowed);
        if (obj != null) {
            obj.put(propChain[propChain.length - 1], value);
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    private static JSONObject lookupObject(JSONObject obj, String[] path, boolean createAllowed) {
        JSONObject node = obj;
        if (path.length > 1) {
            for (int i = 0; i < path.length - 1; i++) {
                JSONValue v = node.get(path[i]);
                if (v != null) {
                    node = v.isObject();
                }
                else if (createAllowed) {
                    JSONObject childNode = new JSONObject();
                    node.put(path[i], childNode);
                    node = childNode;
                }
                if (node == null) {
                    break;
                }
            }
        }
        
        return node;
    }
    
}
