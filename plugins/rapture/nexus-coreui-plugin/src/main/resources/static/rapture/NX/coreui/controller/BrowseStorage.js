/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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
 * Browse repository / storage controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.BrowseStorage', {
  extend: 'Ext.app.Controller',

  views: [
    'repository.RepositoryBrowseStorage',
    'repository.RepositoryBrowseStorageTree'
  ],
  refs: [
    {
      ref: 'feature',
      selector: 'nx-coreui-repository-browse-feature'
    },
    {
      ref: 'panel',
      selector: 'nx-coreui-repository-browse-storage'
    },
    {
      ref: 'tree',
      selector: 'nx-coreui-repository-browse-storage-tree'
    }
  ],

  /**
   * @private
   * Roots cache.
   */
  roots: Ext.create('Ext.util.MixedCollection'),

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'repository-item-type-default': {
        file: 'file_extension_default.png',
        variants: ['x16', 'x24', 'x32']
      },
      'repository-item-type-md5': {
        file: 'file_extension_checksum.png',
        variants: ['x16', 'x32']
      },
      'repository-item-type-jar': {
        file: 'file_extension_jar.png',
        variants: ['x16', 'x32']
      },
      'repository-item-type-pom': {
        file: 'file_extension_xml.png',
        variants: ['x16', 'x32']
      },
      'repository-item-type-sha1': {
        file: 'file_extension_checksum.png',
        variants: ['x16', 'x32']
      },
      'repository-item-type-xml': {
        file: 'file_extension_xml.png',
        variants: ['x16', 'x32']
      },
      'repository-item-type-zip': {
        file: 'file_extension_zip.png',
        variants: ['x16', 'x32']
      }
    });

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.onRefresh
        }
      },
      component: {
        'nx-coreui-repository-browse-list': {
          selection: me.onSelection
        },
        'nx-coreui-repository-browse-storage-tree': {
          select: me.onItemSelected,
          beforeitemexpand: me.onBeforeItemExpand
        }
      }
    });
  },

  onRefresh: function () {
    var me = this,
        panel = me.getPanel();

    if (panel) {
      me.roots.removeAll();
      me.setModel(me.model);
    }
  },

  onSelection: function (list, model) {
    var me = this,
        panel = me.getPanel();

    if (!panel) {
      me.getFeature().addTab({ xtype: 'nx-coreui-repository-browse-storage', title: 'Storage' });
    }
    me.setModel(model);
  },

  setModel: function (model) {
    var me = this,
        tree = me.getTree(),
        root = me.roots.get(model.getId());

    me.model = model;
    if (!root) {
      me.roots.add({
        id: model.getId(),
        node: tree.getStore().setRootNode({
          text: model.get('name')
        })
      });
      tree.getStore().getRootNode().collapse();
    }
    else {
      tree.getStore().setRootNode(root.node);
    }
  },

  onItemSelected: function (tree, node) {
    var me = this,
        panel = me.getPanel();

    if (node.isRoot()) {
      panel.fireEvent('itemdeselected', panel);
    }
    else {
      panel.fireEvent('itemselected', panel, node);
    }
  },

  onBeforeItemExpand: function (node) {
    var me = this;

    if (!node.processed) {
      node.processed = true;
      NX.direct.coreui_RepositoryStorage.read(me.model.getId(), node.getPath('name'), function (response) {
        if (Ext.isDefined(response) && response.success && response.data && response.data.length) {
          Ext.suspendLayouts();
          node.appendChild(response.data);
          Ext.resumeLayouts(true);
        }
      });
    }
  }

});