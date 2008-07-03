package org.sonatype.nexus.gwt.ui.client.table;

public interface CellRenderer {

	Object renderCell(int rowIndex, int colIndex, Object cellValue);
	
}
