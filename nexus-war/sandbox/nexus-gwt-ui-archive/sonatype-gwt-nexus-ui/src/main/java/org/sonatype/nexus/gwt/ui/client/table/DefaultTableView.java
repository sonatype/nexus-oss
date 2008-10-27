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
