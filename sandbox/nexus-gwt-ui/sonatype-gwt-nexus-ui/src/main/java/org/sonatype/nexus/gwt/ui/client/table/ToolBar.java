package org.sonatype.nexus.gwt.ui.client.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sonatype.nexus.gwt.ui.client.constants.ToolBarConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;


public class ToolBar extends HorizontalPanel implements ClickListener,
		SelectionListener {

	private static final ToolBarConstants CONSTANTS
			= (ToolBarConstants) GWT.create(ToolBarConstants.class);

	private ToolBarButton refreshButton = new ToolBarButton(
			"images/view-refresh.png", "images/view-refresh-disabled.png",
			CONSTANTS.refresh(), true);

	private ToolBarButton addButton = new ToolBarButton(
			"images/document-new.png", "images/document-new-disabled.png",
			CONSTANTS.add(), true);

	private ToolBarButton editButton = new ToolBarButton("images/edit.png",
			"images/edit-disabled.png", CONSTANTS.edit(), true);

	private ToolBarButton deleteButton = new ToolBarButton(
			"images/user-trash.png", "images/user-trash-disabled.png",
			CONSTANTS.delete(), true);

	private final static int COMMAND_REFRESH = 1;

	private final static int COMMAND_ADD = 2;

	private final static int COMMAND_EDIT = 3;

	private final static int COMMAND_DELETE = 4;

	private List listeners = new ArrayList();

	public ToolBar() {
		// Hook up events.
		refreshButton.addClickListener(this);
		addButton.addClickListener(this);
		editButton.addClickListener(this);
		deleteButton.addClickListener(this);

		// Set the style of the panel
		setStyleName("toolbar");
		setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

		// Edit and delete are disabled by default
		editButton.setEnabled(false);
		deleteButton.setEnabled(false);

		// Add the widgets to the panel
		add(refreshButton);
		add(addButton);
		add(editButton);
		add(deleteButton);
	}

	public void onClick(Widget sender) {
		int command;

		if (sender == refreshButton) {
			command = COMMAND_REFRESH;
		} else if (sender == addButton) {
			command = COMMAND_ADD;
		} else if (sender == editButton) {
			command = COMMAND_EDIT;
		} else if (sender == deleteButton) {
			command = COMMAND_DELETE;
		} else {
			return;
		}

		fireToolBarListener(command);
	}

	public void selectionChanged(Table sender) {
		boolean enabled = sender.getSelectedRowIndex() != -1;

		editButton.setEnabled(enabled);
		deleteButton.setEnabled(enabled);
	}

	private void fireToolBarListener(int command) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			ToolBarListener l = (ToolBarListener) i.next();
			switch (command) {
			case COMMAND_REFRESH:
				l.onRefresh(this);
				break;
			case COMMAND_ADD:
				l.onAdd(this);
				break;
			case COMMAND_EDIT:
				l.onEdit(this);
				break;
			case COMMAND_DELETE:
				l.onDelete(this);
				break;
			}
		}
	}

	public void addToolBarListener(ToolBarListener l) {
		listeners.add(l);
	}

	public void removeToolBarListener(ToolBarListener l) {
		listeners.remove(l);
	}

}
