package org.sonatype.nexus.ext.gwt.ui.client;

import org.sonatype.nexus.ext.gwt.ui.client.reposerver.RepoMaintenancePage;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point of the NexusUI.
 */
public class NexusUI implements EntryPoint {
    
    private ApplicationContext ctx;

    private Text username;
    private Text loginLink;
    private Text version;

    public void onModuleLoad() {
        ctx = ApplicationContext.instance();
        
        Viewport viewport = new Viewport() {
            {
                setLayout(new BorderLayout());
            }
        };

        addHeader(viewport);
        addMenu(viewport);
        addServers();
        addMain(viewport);

        RootPanel.get().add(viewport);
    }

    private void addHeader(LayoutContainer container) {
        LayoutContainer header = new LayoutContainer() {
            {
                setId("st-header");
                addText("Sonatype Nexus").setId("st-logo");
            }
        };

        LayoutContainer rightSide = new LayoutContainer() {
            {
                setId("st-right-side");
            }
        };
        username = new Text() {
            {
                setTagName("span");
                setId("st-username");
            }
        };
        loginLink = new Text() {
            {
                setTagName("span");
                setId("st-login-link");

                // TODO: Find out how to do this
                // Ext.get('login-link').on('click', Sonatype.repoServer.RepoServer.loginHandler, Sonatype.repoServer.RepoServer);
            }
        };
        version = new Text(ctx.getServerVersion()) {
            {
                setId("st-version");
            }
        };
        rightSide.add(username);
        rightSide.add(loginLink);
        rightSide.add(version);
        header.add(rightSide);

        BorderLayoutData headerLayoutData = new BorderLayoutData(LayoutRegion.NORTH) {
            {
                setMargins(new Margins(5, 5, 5, 5));
                setSize(30);
            }
        };

        container.add(header, headerLayoutData);

        updateLoginStatus();
    }

    private void addMenu(LayoutContainer container) {
        ContentPanel menu = new ContentPanel() {
            {
                setHeading("Sonatype Servers");
                setLayout(new FitLayout());
            }
        };

        // Tabs are added by servers
        TabPanel servers = new TabPanel() {
            {
                setId("st-server-tab-panel");
                setBodyBorder(false);

                // TODO: Set these when the library will support them
                // layoutOnTabChange: true
            }
        };
        
        for (ServerType serverType : ctx.getServerTypes()) {
            servers.add(new TabItem(serverType.getName()));
            //TODO: build function list
        }

        menu.add(servers);

        BorderLayoutData menuLayoutData = new BorderLayoutData(LayoutRegion.WEST) {
            {
                setMargins(new Margins(0, 5, 5, 5));
                setSize(185);
                setCollapsible(true);
                setSplit(false);
            }
        };

        container.add(menu, menuLayoutData);
    }

    private void addMain(LayoutContainer container) {
        // TODO: Make this a MainTabPanel
        TabPanel main = new TabPanel() {
            {
                setId("st-main-tab-panel");
                setResizeTabs(true);
                setTabScroll(true);
                setMinTabWidth(110);

                // TODO: Set these when the library will support them
                // deferredRender: false,
                // defaults: {autoScroll: false, closable: true},
                // layoutOnTabChange: true,
            }
        };

        BorderLayoutData mainLayoutData = new BorderLayoutData(LayoutRegion.CENTER) {
            {
                setMargins(new Margins(0, 5, 5, 0));
            }
        };

        addWelcomeTab(main);
        addRepoMaintenanceTab(main);

        container.add(main, mainLayoutData);
    }

    private void addWelcomeTab(TabPanel panel) {
        LayoutContainer welcome = new LayoutContainer() {
            {
                setId("st-welcome-tab");
                setStyleName("st-little-padding");
                addText("Welcome to the Sonatype Nexus Repository Manager.").setTagName("p");
                addText("You may browse and search the repositories using the options on the left. Administrators may login via the link on the top right.").setTagName("p");
            }
        };

        TabItem welcomeTab = new TabItem("Welcome") {
            {
                setClosable(true);
            }
        };
        welcomeTab.add(welcome);

        panel.add(welcomeTab);
    }
    
    private void addRepoMaintenanceTab(TabPanel panel) {
        RepoMaintenancePage repoMaintenancePanel = new RepoMaintenancePage();
        repoMaintenancePanel.init(ctx.getLocalRepoServer());
        TabItem repoMaintenanceTab = new TabItem("Repositories");
        repoMaintenanceTab.setClosable(true);
        repoMaintenanceTab.add(repoMaintenancePanel);
        panel.add(repoMaintenanceTab);
        panel.setSelection(repoMaintenanceTab);
    }
    
    private void addServers() {
/*
    //allow each included sonatype server to setup its tab and events
    var availSvrs = Sonatype.config.installedServers;
    for(var srv in availSvrs) {
      if (availSvrs[srv] && typeof(Sonatype[srv]) != 'undefined') {
        Sonatype[srv][Sonatype.utils.capitalize(srv)].initServerTab();
      }
    }

    Sonatype.view.serverTabPanel.setActiveTab('st-nexus-tab');
*/
    }

    private void updateLoginStatus() {
        if (false) {
            // TODO: Print the user name
            // username.setText(username + ' | ');
            username.show();
            loginLink.setText("Log Out");
        } else {
            username.hide();
            loginLink.setText("Log In");
        }
    }
}
