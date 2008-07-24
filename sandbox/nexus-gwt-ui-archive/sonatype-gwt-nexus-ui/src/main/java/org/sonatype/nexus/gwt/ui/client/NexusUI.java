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
