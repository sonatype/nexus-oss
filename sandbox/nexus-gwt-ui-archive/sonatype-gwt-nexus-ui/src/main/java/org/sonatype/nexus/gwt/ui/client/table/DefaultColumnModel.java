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
