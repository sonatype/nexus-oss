package org.sonatype.nexus.gwt.ui.client.table;

public interface TableView {

	String getTableStyle();
	
	String getRowStyle(int rowIndex, Object rowValue);

	String getSelectedRowStyle();	
	
	String getColumnStyle(int colIndex);
	
	String getCellStyle(int rowIndex, int colIndex, Object cellValue);

}
