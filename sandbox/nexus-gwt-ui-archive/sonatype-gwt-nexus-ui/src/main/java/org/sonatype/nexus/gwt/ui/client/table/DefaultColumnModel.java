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
package org.sonatype.nexus.gwt.ui.client.table;

import java.util.HashMap;
import java.util.Map;

import org.sonatype.nexus.gwt.ui.client.Util;

import com.google.gwt.i18n.client.ConstantsWithLookup;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class DefaultColumnModel implements ColumnModel {

	private String[] headers;

	private Map renderers;

	private CellRenderer cellRenderer;
		
	public DefaultColumnModel(String[] headers) {
		this.headers = headers;
		init();
	}

	public DefaultColumnModel(String[] propNames, ConstantsWithLookup constants) {
		headers = new String[propNames.length];
		for (int i = 0; i < propNames.length; i++) {
			headers[i] = constants.getString(Util.convertToJavaStyle("header." + propNames[i]));
		}
		init();
	}
	
	private void init() {
		renderers = new HashMap();
		cellRenderer = new DefaultCellRenderer();
	}
	
	public Widget getHeader(int colIndex) {
		return new Label(headers != null ? headers[colIndex] : "");
	}

	public void clear() {
		renderers.clear();
	}

	public void addCellRenderer(int colIndex, CellRenderer cellRenderer) {
		renderers.put(new Integer(colIndex), cellRenderer);
	}

	public CellRenderer getCellRenderer(int colIndex) {
		if (renderers.containsKey(new Integer(colIndex))) {
			return (CellRenderer) renderers.get(new Integer(colIndex));
		}
		return cellRenderer;
	}

}
