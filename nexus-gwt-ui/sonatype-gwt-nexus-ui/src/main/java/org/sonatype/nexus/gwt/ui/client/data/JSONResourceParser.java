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
