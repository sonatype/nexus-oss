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

public class DefaultTableView implements TableView {

	final static String TABLE_MAIN_STYLE = "table-main";

	final static String TABLE_HEADER_STYLE = "table-header";

	final static String TABLE_ODD_ROW_STYLE = "table-odd-row";

	final static String TABLE_EVEN_ROW_STYLE = "table-even-row";

	final static String TABLE_SELECTED_ROW_STYLE = "table-selected-row";
	
	final static String TABLE_COLUMN_STYLE_PREFIX = "table-column-";

	public String getTableStyle() {
		return TABLE_MAIN_STYLE;
	}

	public String getRowStyle(int rowIndex, Object rowValue) {
		if (rowIndex == 0) {
			return TABLE_HEADER_STYLE;
		}
		if ((rowIndex & 1) == 1) {
			return TABLE_ODD_ROW_STYLE;
		}
		return TABLE_EVEN_ROW_STYLE;
	}

	public String getSelectedRowStyle() {
		return TABLE_SELECTED_ROW_STYLE;
	}
	
	public String getColumnStyle(int colIndex) {
		return TABLE_COLUMN_STYLE_PREFIX + colIndex;
	}
	
	public String getCellStyle(int rowIndex, int colIndex, Object cellValue) {
		return null;
	}

}
