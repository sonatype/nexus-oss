/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2014 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
/**
 * Browse Index controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.BrowseIndex', {
  extend: 'Ext.app.Controller',

  views: [
    'repository.RepositoryBrowseIndex',
    'repository.RepositoryBrowseIndexTree'
  ],
  refs: [
    {
      ref: 'feature',
      selector: 'nx-coreui-repository-browse-feature'
    },
    {
      ref: 'list',
      selector: 'nx-coreui-repository-browse-list'
    },
    {
      ref: 'panel',
      selector: 'nx-coreui-repository-browse-index'
    },
    {
      ref: 'tree',
      selector: 'nx-coreui-repository-browse-index-tree'
    },
    {
      ref: 'componentDetail',
      selector: 'nx-coreui-component-detail'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.listen({
      component: {
        'nx-coreui-repository-browse-list': {
          selection: me.onSelection
        },
        'nx-coreui-repository-browse-index': {
          activate: me.onActivate,
          deactivate: me.onDeactivate
        },
        'nx-coreui-repository-browse-index-tree': {
          select: me.onNodeSelected,
          beforeitemexpand: me.onBeforeItemExpand
        }
      }
    });
  },

  onActivate: function (panel) {
    var me = this;

    panel.active = true;
    me.buildTree(me.getList().getSelectionModel().getSelection()[0]);
  },

  onDeactivate: function (panel) {
    panel.active = false;
  },

  onSelection: function (repositoryGrid, repositoryModel) {
    var me = this,
        panel = me.getPanel();

    if (repositoryModel.get('indexable') || repositoryModel.get('downloadRemoteIndexes')) {
      if (!panel) {
        panel = me.getFeature().addTab({ xtype: 'nx-coreui-repository-browse-index', title: 'Index' });
      }
      if (panel.active) {
        me.buildTree(repositoryModel);
      }
    }
    else {
      if (panel) {
        me.getFeature().removeTab(panel);
      }
    }
  },

  buildTree: function (repositoryModel) {
    var me = this,
        tree = me.getTree();

    tree.getStore().setRootNode({
      repositoryId: repositoryModel.getId(),
      text: repositoryModel.get('name')
    });
    me.onNodeSelected(tree, tree.getStore().getRootNode());
  },

  onNodeSelected: function (tree, node) {
    var me = this;

    me.getComponentDetail().setComponent(node.isRoot() ? undefined : {
      repositoryId: node.get('repositoryId'),
      uri: node.get('path')
    });
  },

  onBeforeItemExpand: function (node) {
    if (!node.processed) {
      node.processed = true;
      NX.direct.coreui_RepositoryStorage.read(node.get('repositoryId'), node.getPath('name'), function (response) {
        if (Ext.isDefined(response) && response.success && response.data && response.data.length) {
          Ext.suspendLayouts();
          node.appendChild(response.data);
          Ext.resumeLayouts(true);
        }
      });
    }
  }

});