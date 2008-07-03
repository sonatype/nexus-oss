package org.sonatype.nexus.gwt.ui.client;

import com.google.gwt.i18n.client.ConstantsWithLookup;

public class Util {

	public static String convertToJavaStyle(String dotSeparatedName) {
		StringBuffer javaStyleName = new StringBuffer(dotSeparatedName);
		for (int i = 0; i < dotSeparatedName.length(); i++) {
			if (dotSeparatedName.charAt(i) == '.') {
				i++;
				javaStyleName.replace(i, i+1, dotSeparatedName.substring(i, i+1).toUpperCase());
			}
		}
		return javaStyleName.toString().replaceAll("\\.", "");
	}

    // Converting java style name to - delimited
	public static String convertToStyleName(String javaName) {
        StringBuffer styleName = new StringBuffer(javaName.toLowerCase());
        int offset = 0;
        for (int i = 1; i < javaName.length(); i++) {
        	if (Character.isUpperCase(javaName.charAt(i))) {
        		styleName.insert(i + offset, '-');
        		offset++;
        	}
        }
        return styleName.toString();
	}
	
	public static String getStringProperty(ConstantsWithLookup constants, String property) {
	    return constants.getString(convertToJavaStyle(property));
	}
	
}
