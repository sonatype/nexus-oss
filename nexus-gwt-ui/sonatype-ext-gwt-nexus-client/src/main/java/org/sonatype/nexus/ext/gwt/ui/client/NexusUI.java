package org.sonatype.nexus.ext.gwt.ui.client;

import org.sonatype.gwt.client.resource.Variant;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.XMLParser;

/**
 * Entry point of the NexusUI.
 */
public class NexusUI implements EntryPoint {
    
    private ApplicationContext ctx;

    private Text username;
    private Text loginLink;
    private Text version;

    private TabPanel tabPanel;
    
    public void onModuleLoad() {
        ctx = ApplicationContext.instance();
        
        Viewport viewport = new Viewport() {
            {
                setLayout(new BorderLayout());
            }
        };

        addHeader(viewport);
        addMenu(viewport);
        addTabPanel(viewport);

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

                // TODO: Find out how to do authentication
                // Ext.get('login-link').on('click', Sonatype.repoServer.RepoServer.loginHandler, Sonatype.repoServer.RepoServer);
            }
        };
        version = new Text() {
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

        getServerVersion();
        updateLoginStatus();
    }

    private void getServerVersion() {
        ctx.getLocalRepoServer().getResource("status").get(new RequestCallback() {
            public void onError(Request request, Throwable exception) {
                version.setText("Version unavailable");
            }
            public void onResponseReceived(Request request, Response response) {
                Document doc = XMLParser.parse(response.getText());
                String ver = doc.getElementsByTagName("version").item(0).getFirstChild().getNodeValue();
                version.setText(ver);
            }
        }, Variant.APPLICATION_XML);
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
            }
        };
        
        for (ServerType serverType : ctx.getServerTypes()) {
            addServer(servers, serverType);
        }
        
        servers.setSelection(servers.getItem(0));

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

    private void addServer(TabPanel servers, ServerType serverType) {
        final String serverID = convertToStyleName(serverType.getName());
        
        TabItem instances = new TabItem(serverType.getName()) {
            {
                setId("st-server-" + serverID + "-tab");
            }
        };
        
        for (ServerInstance serverInstance: serverType.getInstances()) {
            addServerInstance(instances, serverType, serverInstance);
        }
        
        servers.add(instances);
    }
    
    private void addServerInstance(TabItem instances, ServerType serverType,
            ServerInstance serverInstance) {
        instances.removeAll();
        
        ContentPanel groups = new ContentPanel() {
            {
                addStyleName("st-server-instance-panel");
                setHeaderVisible(false);
                setLayout(new FitLayout());
                setBodyBorder(false);
            }
        };

        for (ServerFunctionGroup serverFunctionGroup: serverType.getFunctionGroups()) {
            addServerFunctionGroup(groups, serverFunctionGroup);
        }

        instances.add(groups);
        
        if (instances.isRendered()) {
            instances.layout();
        }
    }

    private void addServerFunctionGroup(ContentPanel groups, final ServerFunctionGroup serverFunctionGroup) {
        final ContentPanel functions = new ContentPanel() {
            {
                setHeading(serverFunctionGroup.getName());
                addStyleName("st-server-group-panel");
                setLayout(new FitLayout());
                setFrame(true);
            }
        };
        
        for (ServerFunction serverFunction: serverFunctionGroup.getFunctions()) {
            addServerFunction(functions, serverFunction);
        }
        
        groups.add(functions);
    }

    private void addServerFunction(ContentPanel functions, final ServerFunction serverFunction) {
        functions.add(new Link(serverFunction.getMenuName()) {
            public void onClick(ComponentEvent event) {
                   addServerFunctionTab(serverFunction);
            }
        });
    }

    private void addTabPanel(LayoutContainer container) {
        tabPanel = new TabPanel() {
            {
                setId("st-main-tab-panel");
                setResizeTabs(true);
                setTabScroll(true);
                setMinTabWidth(110);
            }
        };

        BorderLayoutData tabPanelLayoutData = new BorderLayoutData(LayoutRegion.CENTER) {
            {
                setMargins(new Margins(0, 5, 5, 0));
            }
        };

        container.add(tabPanel, tabPanelLayoutData);

        addWelcomeTab();
    }

    private void addWelcomeTab() {
        if (activateTab("st-welcome-tab")) {
            return;
        }

        LayoutContainer welcome = new LayoutContainer() {
            {
                setStyleName("st-little-padding");
                addText("Welcome to the Sonatype Nexus Repository Manager.").setTagName("p");
                addText("You may browse and search the repositories using the options on the left. Administrators may login via the link on the top right.").setTagName("p");
            }
        };
        addTab(welcome, "Welcome", "st-welcome-tab");
    }
    
    private void addServerFunctionTab(ServerFunction serverFunction) {
        String id = "st-" + convertToStyleName(serverFunction.getTabName()) + "-tab";
        if (activateTab(id)) {
            return;
        }
        
        addTab(serverFunction.getPanel(ctx.getLocalRepoServer()),
                serverFunction.getTabName(), id);
    }
    
    private boolean activateTab(String id) {
        TabItem tabItem = tabPanel.findItem(id, false);
        if (tabItem == null) {
            return false;
        }

        tabPanel.setSelection(tabItem);

        return true;
    }
    
    private void addTab(final Container<Component> container, String title, final String id) {
        TabItem tabItem = new TabItem(title) {
            {
                setId(id);
                setClosable(true);
                add(container);
            }
        };

        tabPanel.add(tabItem);
        tabPanel.setSelection(tabItem);
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

    // TODO: Move this to a utility class
    public static String convertToStyleName(String javaName) {
        StringBuffer styleName = new StringBuffer(javaName.toLowerCase());
        int offset = 0;
        
        for (int i = 1; i < javaName.length(); i++) {
            if (Character.isUpperCase(javaName.charAt(i))) {
                styleName.insert(i + offset, '-');
                offset++;
            }
        }
        
        return styleName.toString();
    }
    
}
