/*
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