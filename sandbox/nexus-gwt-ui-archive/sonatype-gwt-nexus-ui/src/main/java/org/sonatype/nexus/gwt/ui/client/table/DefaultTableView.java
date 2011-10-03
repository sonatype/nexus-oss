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
