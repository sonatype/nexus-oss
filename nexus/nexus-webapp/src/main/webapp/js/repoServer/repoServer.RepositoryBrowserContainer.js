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
// This container will host both the repository browser and the artifact
// information panel
Sonatype.repoServer.RepositoryBrowserContainer = function(config) {
  var config = config || {};
  var defaultConfig = {
    artifactContainerInitEvent : 'fileContainerInit',
    artifactContainerUpdateEvent : 'fileContainerUpdate'
  };
  Ext.apply(this, config, defaultConfig);

  var items = [];

  this.repositoryBrowser = new Sonatype.repoServer.RepositoryBrowsePanel({
        name : 'repositoryBrowser',
        payload : this.payload,
        tabTitle : this.tabTitle,
        browseIndex : false,
        region : 'center',
        nodeClickEvent : 'fileNodeClickedEvent',
        nodeClickPassthru : {
          container : this
        }
      });

  this.artifactContainer = new Sonatype.repoServer.ArtifactContainer({
        collapsible : true,
        collapsed : true,
        region : 'east',
        split : true,
        width : '50%',
        initEventName : this.artifactContainerInitEvent,
        updateEventName : this.artifactContainerUpdateEvent
      });

  items.push(this.repositoryBrowser);
  items.push(this.artifactContainer);

  Sonatype.repoServer.RepositoryBrowserContainer.superclass.constructor.call(this, {
        layout : 'border',
        // this hideMode causes the tab to properly render when coming back from
        // hidden
        hideMode : 'offsets',
        items : items
      });
};

Ext.extend(Sonatype.repoServer.RepositoryBrowserContainer, Ext.Panel, {
      updatePayload : function(payload) {

        if (payload == null)
        {
          this.collapse();
          this.repositoryBrowser.updatePayload(null);
          this.artifactContainer.collapsePanel();
        }
        else
        {
          this.expand();
          this.repositoryBrowser.updatePayload(payload);
          this.artifactContainer.updateArtifact(payload);
        }

      }
    });

// Add the browse storage and browse index panels to the repo
Sonatype.Events.addListener('repositoryViewInit', function(cardPanel, rec) {
      if (rec.data.resourceURI)
      {
        cardPanel.add(new Sonatype.repoServer.RepositoryBrowserContainer({
              payload : rec,
              name : 'browsestorage',
              tabTitle : 'Browse Storage'
            }));
      }
    });

Sonatype.Events.addListener('fileNodeClickedEvent', function(node, passthru) {
      if (passthru && passthru.container && passthru.container.artifactContainer.items.getCount() > 0)
      {
        if (node && node.isLeaf())
        {
          passthru.container.artifactContainer.updateArtifact({
                text : node.attributes.text,
                leaf : node.attributes.leaf,
                resourceURI : node.attributes.resourceURI
              });
        }
        else
        {
          passthru.container.artifactContainer.collapse();
        }
      }
    });
