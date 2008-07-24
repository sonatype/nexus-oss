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
