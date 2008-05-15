package org.sonatype.nexus.gwt.ui.client.table;

import org.sonatype.nexus.gwt.ui.client.constants.PagingNavBarConstants;
import org.sonatype.nexus.gwt.ui.client.constants.PagingNavBarMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


public class PagingNavBar extends HorizontalPanel implements TableModelListener, ClickListener {

    private static final PagingNavBarConstants CONSTANTS = (PagingNavBarConstants) GWT.create(PagingNavBarConstants.class);
    private static final PagingNavBarMessages MESSAGES = (PagingNavBarMessages) GWT.create(PagingNavBarMessages.class);

    private Label countLabel = new Label();
    private ToolBarButton firstPageButton = new ToolBarButton("images/go-first.png", "images/go-first-disabled.png", CONSTANTS.firstPage(), false);
    private ToolBarButton previousPageButton = new ToolBarButton("images/go-previous.png", "images/go-previous-disabled.png", CONSTANTS.previousPage(), false);
    private ToolBarButton nextPageButton = new ToolBarButton("images/go-next.png", "images/go-next-disabled.png", CONSTANTS.nextPage(), false);
    private ToolBarButton lastPageButton = new ToolBarButton("images/go-last.png", "images/go-last-disabled.png", CONSTANTS.lastPage(), false);

    private PageableTableModel model;
    
    private boolean alwaysShowNavButtons;
    
    public PagingNavBar(PageableTableModel model) {
    	this(model, false);
    }
    
    public PagingNavBar(PageableTableModel model, boolean alwaysShowNavButtons) {
        this.model = model;
        this.alwaysShowNavButtons = alwaysShowNavButtons;

        // Hook up events.
        firstPageButton.addClickListener(this);
        previousPageButton.addClickListener(this);
        nextPageButton.addClickListener(this);
        lastPageButton.addClickListener(this);
        model.addTableModelListener(this);

        // Set the style of the panel
        setStyleName("navbar");
        setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        // Add the widgets to the panel
        add(firstPageButton);
        add(previousPageButton);
        add(countLabel);
        add(nextPageButton);
        add(lastPageButton);

        update();
    }

    public void modelChanged(TableModel model) {
        update();
    }

    public void onClick(Widget sender) {
        if (sender == nextPageButton) {
            model.nextPage();
        } else if (sender == previousPageButton) {
            model.prevPage();
        } else if (sender == firstPageButton) {
            model.firstPage();
        } else if (sender == lastPageButton) {
            model.lastPage();
        }
        update();
    }

    private void update() {
        boolean visible = model.getPageCount() > 1;
        boolean onFirstPage = model.getPageIndex() == 0;
        boolean onLastPage = model.getPageIndex() >= model.getPageCount() - 1;

        this.setVisible(visible || alwaysShowNavButtons);

        firstPageButton.setEnabled(!onFirstPage);
        previousPageButton.setEnabled(!onFirstPage);
        nextPageButton.setEnabled(!onLastPage);
        lastPageButton.setEnabled(!onLastPage);

        int page = 0;
        if (model.getPageCount() > 0) {
            page = model.getPageIndex() + 1;
        }
        countLabel.setText(MESSAGES.pageOf(page, model.getPageCount()));
    }

}
