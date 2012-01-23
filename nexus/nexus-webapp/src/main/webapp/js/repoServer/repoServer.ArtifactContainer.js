/*
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
Sonatype.repoServer.ArtifactContainer = function(config) {
  var config = config || {};
  var defaultConfig = {
    initEventName : 'artifactContainerInit',
    updateEventName : 'artifactContainerUpdate'
  };
  Ext.apply(this, config, defaultConfig);

  Sonatype.repoServer.ArtifactContainer.superclass.constructor.call(this, {
        layoutOnTabChange : true
      });

  var items = [];

  Sonatype.Events.fireEvent(this.initEventName, items, null);

  items.sort(function(a, b) {
        if (a.preferredIndex == undefined && b.preferredIndex == undefined)
        {
          return 0;
        }

        if (a.preferredIndex == undefined)
        {
          return 1;
        }
        else if (b.preferredIndex == undefined)
        {
          return -1;
        }
        else if (a.preferredIndex < b.preferredIndex)
        {
          return -1;
        }
        else if (a.preferredIndex > b.preferredIndex)
        {
          return 1;
        }
        else
        {
          return 0;
        }
      });

  for (var i = 0; i < items.length; i++)
  {
    this.add(items[i]);
  }
};

Ext.extend(Sonatype.repoServer.ArtifactContainer, Sonatype.panels.AutoTabPanel, {
      collapsePanel : function() {
        this.collapse();
        Sonatype.Events.fireEvent(this.updateEventName, this, null);
      },
      updateArtifact : function(data) {
        Sonatype.Events.fireEvent(this.updateEventName, this, data);
        if (data != null)
        {
          this.expand();
        }
      },
      hideTab : function(panel) {
      	panel.tabHidden = true;
        this.tabPanel.hideTabStripItem(panel);
        for (var i = 0; i < this.tabPanel.items.getCount(); i++)
        {
          var nextPanel = this.tabPanel.items.get(i);
          if (nextPanel.id != panel.id && !nextPanel.tabHidden)
          {
            this.tabPanel.setActiveTab(nextPanel);
            return;
          }
        }

        // we haven't found anything, so collapse
        this.tabPanel.doLayout();
        this.collapse();
      },
      showTab : function(panel) {
      	panel.tabHidden = false;
        this.tabPanel.unhideTabStripItem(panel);
      }

    });