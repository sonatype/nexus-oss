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
