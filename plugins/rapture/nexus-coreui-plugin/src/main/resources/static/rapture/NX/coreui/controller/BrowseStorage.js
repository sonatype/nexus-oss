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
 * Browse storage controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.BrowseStorage', {
  extend: 'Ext.app.Controller',

  views: [
    'repositorybrowse.BrowseStorage',
    'repositorybrowse.BrowseStorageTree',
    'repositorybrowse.StorageFileContainer'
  ],
  refs: [
    { ref: 'tree', selector: 'nx-coreui-repositorybrowse-storage-tree' },
    { ref: 'storageFileContainer', selector: 'nx-coreui-repositorybrowse-storagefilecontainer' }
  ],

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'repository-item-type-default': { file: 'file_extension_default.png', variants: ['x16', 'x24', 'x32'] },
      'repository-item-type-md5': { file: 'file_extension_checksum.png', variants: ['x16', 'x32'] },
      'repository-item-type-jar': { file: 'file_extension_jar.png', variants: ['x16', 'x32'] },
      'repository-item-type-pom': { file: 'file_extension_xml.png', variants: ['x16', 'x32'] },
      'repository-item-type-sha1': { file: 'file_extension_checksum.png', variants: ['x16', 'x32'] },
      'repository-item-type-xml': { file: 'file_extension_xml.png', variants: ['x16', 'x32'] },
      'repository-item-type-zip': { file: 'file_extension_zip.png', variants: ['x16', 'x32'] }
    });

    me.listen({
      component: {
        'nx-coreui-repositorybrowse-list': {
          selection: me.onSelection
        },
        'nx-coreui-repositorybrowse-storage-tree': {
          select: me.onNodeSelected,
          beforeitemexpand: me.loadChildren,
          itemclick: me.onItemClick
        }
      }
    });
  },

  /**
   * @private
   * (Re)build tree for selected repository.
   */
  onSelection: function(list, repositoryModel) {
    if (repositoryModel) {
      this.buildTree(repositoryModel);
    }
  },

  /**
   * @private
   * (Re)build tree for selected repository.
   */
  buildTree: function(repositoryModel) {
    var me = this,
        tree = me.getTree();

    tree.getStore().setRootNode({
      repositoryId: repositoryModel.getId(),
      path: '/',
      text: repositoryModel.get('name')
    });
    me.onNodeSelected(tree, tree.getStore().getRootNode());
  },

  /**
   * @private
   * When a node gets selected, refresh storage file in right-side container.
   */
  onNodeSelected: function(tree, node) {
    var me = this;
    me.getStorageFileContainer().showStorageFile(
        node.get('repositoryId'),
        node.isLeaf() ? node.get('path') : undefined
    );
  },

  /**
   * @private
   * Auto expand nodes on click.
   */
  onItemClick: function(tree, node) {
    if (!node.isLeaf()) {
      this.getTree().expandNode(node);
    }
  },

  /**
   * @private
   * Load children of selected node, if not already loaded.
   */
  loadChildren: function(node) {
    if (!node.processed) {
      node.processed = true;
      NX.direct.coreui_RepositoryStorage.readChildren(node.get('repositoryId'), node.get('path'), function(response) {
        if (Ext.isDefined(response) && response.success && response.data && response.data.length) {
          Ext.suspendLayouts();
          node.appendChild(response.data);
          Ext.resumeLayouts(true);
        }
      });
    }
  }

});