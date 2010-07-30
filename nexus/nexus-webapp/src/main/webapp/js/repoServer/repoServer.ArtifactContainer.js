/*
 * Sonatype Nexus (TM) Open Source Version. Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at
 * http://nexus.sonatype.org/dev/attributions.html This program is licensed to
 * you under Version 3 only of the GNU General Public License as published by
 * the Free Software Foundation. This program is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License Version 3 for more details. You should have received a copy of
 * the GNU General Public License Version 3 along with this program. If not, see
 * http://www.gnu.org/licenses/. Sonatype Nexus (TM) Professional Version is
 * available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc.
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