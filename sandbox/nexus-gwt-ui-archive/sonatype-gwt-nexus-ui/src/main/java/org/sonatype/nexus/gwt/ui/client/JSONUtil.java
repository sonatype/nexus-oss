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
