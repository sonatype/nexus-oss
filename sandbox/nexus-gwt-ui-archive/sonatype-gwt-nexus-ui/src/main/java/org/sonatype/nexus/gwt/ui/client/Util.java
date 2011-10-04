/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
