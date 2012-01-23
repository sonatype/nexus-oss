/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.gwt.ui.client.feed;

import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.resource.Representation;
import org.sonatype.nexus.gwt.client.Nexus;
import org.sonatype.nexus.gwt.ui.client.data.JSONArrayDataStore;
import org.sonatype.nexus.gwt.ui.client.table.CellRenderer;
import org.sonatype.nexus.gwt.ui.client.table.DefaultColumnModel;
import org.sonatype.nexus.gwt.ui.client.table.JSONArrayTableModel;
import org.sonatype.nexus.gwt.ui.client.table.Table;

import com.google.gwt.http.client.Request;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 *
 * @author barath
 */
public class FeedsPage extends VerticalPanel {
    
    private JSONArrayDataStore dataStore = new JSONArrayDataStore();
    
    private JSONArrayTableModel model;
    
    private class FeedResponseHandler implements EntityResponseHandler {
        
        public void onSuccess(Representation entity) {
            JSONValue json = (JSONValue) entity.getParsed();
            JSONArray feeds = json.isObject().get("data").isArray();
            dataStore.setElements(feeds);
        }

        public void onError(Request request, Throwable error) {
            Window.alert( "Error! Message: " + error.getMessage() );
        }
        
    }
    
    public FeedsPage() {
        model = new JSONArrayTableModel(dataStore, new String[]{"name", "resourceURI"}) {
            public Object getCell(int rowIndex, int colIndex) {
                Object cellValue = super.getCell(rowIndex, colIndex);
                if (colIndex == 1) {
                    cellValue = "http://localhost:8081" + cellValue;
                }
                return cellValue;
            }
        };
        
        Table table = new Table(model, 0);
        
        DefaultColumnModel columnModel =
            new DefaultColumnModel(new String[]{"System Feeds", ""});
        
        columnModel.addCellRenderer(1, new CellRenderer() {
            public Object renderCell(int rowIndex, int colIndex, Object cellValue) {
                Hyperlink link = new Hyperlink("Read", true, (String) cellValue);
                link.addClickListener(new ClickListener() {
                    public void onClick(Widget sender) {
                        Window.alert("TODO: Open the link in new tab/window");
                    }
                });
                return link;
            }
        });
        
        table.setColumnModel(columnModel);
        table.setRowSelectionEnabled(false);
        table.setWidth("100%");
        
        setStyleName("repo-list");
        add(table);
        
        new Nexus().getLocalInstance()
            .getFeedsService().listFeeds(new FeedResponseHandler());
    }

}
