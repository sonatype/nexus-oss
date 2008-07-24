package org.sonatype.nexus.gwt.ui.client.table;

import com.google.gwt.user.client.ui.Widget;

public interface ColumnModel {

	Widget getHeader(int colIndex);
		
	CellRenderer getCellRenderer(int colIndex);

}
