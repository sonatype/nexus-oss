package org.sonatype.nexus.gwt.ui.client.table;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.Image;

public class ImageCellRenderer implements CellRenderer {

	Map images;
	
	public ImageCellRenderer() {
		images = new HashMap();
	}

	public void clear() {
		images.clear();
	}
	
	public void addMapping(Object cellValue, Image cellImage) {
		images.put(cellValue, cellImage);
	}
	
	public Object renderCell(int rowIndex, int colIndex, Object cellValue) {
		return images.get(cellValue);
	}

}
