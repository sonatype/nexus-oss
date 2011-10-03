/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.gwt.ext.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.gwtext.client.core.Position;
import com.gwtext.client.core.RegionPosition;
import com.gwtext.client.widgets.BoxComponent;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.DefaultsHandler;
import com.gwtext.client.widgets.Panel;
import com.gwtext.client.widgets.TabPanel;
import com.gwtext.client.widgets.Viewport;
import com.gwtext.client.widgets.layout.BorderLayout;
import com.gwtext.client.widgets.layout.BorderLayoutData;
import com.gwtext.client.widgets.layout.FitLayout;

/**
 * Entry point of the NexusUI.
 */
public class NexusUI implements EntryPoint {

    public void onModuleLoad() {
        Panel main = new Panel() {
            {
                setLayout(new BorderLayout());
            }
        };

        /* Create the header */
        BoxComponent header = new BoxComponent() {
            {
                setEl("header");
                setAutoHeight(true);
            }
        };

        BorderLayoutData headerLayoutData
          = new BorderLayoutData(RegionPosition.NORTH) {
            {
                setMargins(5, 5, 5, 5);
            }
        };

        main.add(header, headerLayoutData);

        /* Create menu */
        TabPanel menu = new TabPanel() {
            {
                setId("st-server-tab-panel");
                setBorder(false);
                setLayoutOnTabChange(true);
                setTabPosition(Position.TOP);
            }
        };

        /* Create menu container */
        Panel menuContainer = new Panel() {
            {
                setTitle("Sonatype Servers");
                setCollapsible(true);
                setLayout(new FitLayout());
                setWidth(185);

            }
        };

        menuContainer.add(menu);

        BorderLayoutData menuContainerLayoutData
          = new BorderLayoutData(RegionPosition.WEST) {
            {
                setMargins(0, 5, 5, 5);
                setMinSize(185);
                setMaxSize(185);
                setSplit(false);
            }
        };

        main.add(menuContainer, menuContainerLayoutData);

        /* Create main tab panel */
        TabPanel mainTabPanel = new TabPanel() { // TODO: Make this a MainPanelTab
            {
                setId("st-main-tab-panel");
                setActiveTab(0);
                setDeferredRender(false);
                setEnableTabScroll(true);
                setLayoutOnTabChange(true);
                setMinTabWidth(110);
                setResizeTabs(true);
                setDefaults(new DefaultsHandler() {
                    public void apply(Component component) {
                        ((Panel)component).setAutoScroll(false);
                        ((Panel)component).setClosable(false);
                    }
                });
            }
        };

        // TODO: Add the welcome tab
        
        mainTabPanel.add(new RepoMaintenancePanel());

        BorderLayoutData mainTabPanelLayoutData
          = new BorderLayoutData(RegionPosition.CENTER) {
            {
                setMargins(0, 5, 5, 0);
            }
        };

        main.add(mainTabPanel, mainTabPanelLayoutData);

        new Viewport(main);
    }

}
