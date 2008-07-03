package org.sonatype.nexus.gwt.ui.client.table;


public interface ToolBarListener {

	void onRefresh(ToolBar sender);
	
	void onAdd(ToolBar sender);
	
	void onEdit(ToolBar sender);
	
	void onDelete(ToolBar sender);
	
}
