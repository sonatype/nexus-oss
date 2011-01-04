/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.gwt.ui.client;

import org.sonatype.nexus.gwt.client.Nexus;
import org.sonatype.nexus.gwt.ui.client.data.JSONArrayDataStore;
import org.sonatype.nexus.gwt.ui.client.feed.FeedsPage;
import org.sonatype.nexus.gwt.ui.client.repository.HostedRepositoryPage;
import org.sonatype.nexus.gwt.ui.client.repository.ProxyRepositoryPage;
import org.sonatype.nexus.gwt.ui.client.repository.RepositoriesPage;
import org.sonatype.nexus.gwt.ui.client.repository.RepositoryPage;
import org.sonatype.nexus.gwt.ui.client.repository.VirtualRepositoryPage;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;

/**
 * Entry point of the NexusUI.
 */
public class NexusUI implements EntryPoint {
    
    public static Nexus server = new Nexus();
    
    private static JSONArrayDataStore repositories = new JSONArrayDataStore();
    
    private static TabPanel tabPanel = new TabPanel();
    
    private static DeckPanel deck = new DeckPanel();
    
    private static RepositoriesPage repositoriesPage = new RepositoriesPage(repositories);
    
    private static FeedsPage feedsPage = new FeedsPage();
    
    public void onModuleLoad() {
        deck.add(repositoriesPage);
        deck.add(new ProxyRepositoryPage().initComponents());
        deck.add(new HostedRepositoryPage().initComponents());
        deck.add(new VirtualRepositoryPage(repositories).initComponents());
        
        tabPanel.add(deck, "Repositories");
        tabPanel.add(feedsPage, "Feeds");
        tabPanel.setWidth("100%");
        RootPanel.get().add(tabPanel);
        
        openRepositoriesPage();
    }
    
    public static void openRepositoriesPage() {
        deck.showWidget(0);
        tabPanel.selectTab(0);
    }
    
    public static void openEditRepositoryPage(Object formData) {
        openRepositoryPage(formData, true);
    }
    
    public static void openCreateRepositoryPage(Object formData) {
        JSONObject obj = (JSONObject) formData;
        JSONUtil.setValue(obj, "repoType", new JSONString("virtual"));
        openRepositoryPage(formData, false);
    }
    
    public static void openRepositoryPage(Object formData, boolean edit) {
        String repoType = ((JSONObject) formData).get("repoType").isString().stringValue();
        int index = 0;
        if ("proxy".equals(repoType)) {
            index = 1;
        } else if ("hosted".equals(repoType)) {
            index = 2;
        } else if ("virtual".equals(repoType)) {
            index = 3;
        }
        ((RepositoryPage) deck.getWidget(index)).load(formData);
        ((RepositoryPage) deck.getWidget(index)).setEditMode(edit);
        deck.showWidget(index);
    }
    
    public static void openFeedsPage() {
        tabPanel.selectTab(1);
    }
    
}
