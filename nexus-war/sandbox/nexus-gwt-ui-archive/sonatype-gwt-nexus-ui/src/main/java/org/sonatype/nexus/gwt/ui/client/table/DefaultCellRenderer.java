package org.sonatype.nexus.gwt.ui.client.table;

public class DefaultCellRenderer implements CellRenderer {

	public Object renderCell(int rowIndex, int colIndex, Object cellValue) {
		return cellValue == null ? "" : cellValue.toString();
	}

}
