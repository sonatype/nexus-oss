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
    { ref: 'storageFileContainer', selector: 'nx-coreui-repositorybrowse-storage nx-coreui-repositorybrowse-storagefilecontainer' }
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
          itemclick: me.onItemClick,
          beforeitemcontextmenu: me.showContextMenu,
          beforecontextmenushow: me.fillContextMenu
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

    tree.repository = repositoryModel;
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
    if (!node.get('processed')) {
      node.set('processed', true);
      NX.direct.coreui_RepositoryStorage.readChildren(node.get('repositoryId'), node.get('path'), function(response) {
        if (Ext.isDefined(response) && response.success && response.data && response.data.length) {
          Ext.suspendLayouts();
          node.appendChild(response.data);
          node.sort(function(n1, n2) {
            var t1 = n1.get('text') || '',
                t2 = n2.get('text') || '';

            if (n1.isLeaf() !== n2.isLeaf()) {
              return n1.isLeaf() ? 1 : -1;
            }
            return t1.localeCompare(t2);
          }, true);
          Ext.resumeLayouts(true);
        }
      });
    }
  },

  /**
   * @private
   * Show context menu.
   */
  showContextMenu: function(view, node, item, index, event) {
    var me = this,
        tree = me.getTree(),
        menu = Ext.create('Ext.menu.Menu');

    tree.fireEvent('beforecontextmenushow', menu, tree.repository, node);
    event.stopEvent();
    if (menu.items.length) {
      menu.showAt(event.getXY());
      return false;
    }
    return true;
  },

  /**
   * @private
   * Fill context menu with default menu items.
   */
  fillContextMenu: function(menu, repository, node) {
    var me = this;

    if (NX.Permissions.check('nexus:cache', 'delete')
        && repository.get('type') !== 'virtual' && repository.get('userManaged')) {
      menu.add({
        text: 'Expire Cache',
        handler: Ext.bind(me.expireCache, me, [repository, node.get('path')])
      });
    }
    if (NX.Permissions.check('nexus:metadata', 'delete')
        && (repository.get('format') === 'maven1' || repository.get('format') === 'maven2' )
        && (repository.get('type') === 'hosted' || repository.get('type') === 'group' )
        && repository.get('userManaged')) {
      menu.add({
        text: 'Rebuild Metadata',
        handler: Ext.bind(me.rebuildMavenMetadata, me, [repository, node.get('path')])
      });
    }
    if (node.isLeaf() && repository.get('type') === 'proxy') {
      menu.add({
        text: 'Download From Remote',
        handler: Ext.bind(me.downloadPath, me, [repository.get('remoteStorageUrl'), node.get('path')])
      });
    }
    if (!node.isRoot() && !node.isLeaf() && repository.get('type') === 'proxy') {
      menu.add({
        text: 'View Remote',
        handler: Ext.bind(me.downloadPath, me, [repository.get('remoteStorageUrl'), node.get('path')])
      });
    }
    if (node.isLeaf()) {
      menu.add({
        text: 'Download',
        handler: Ext.bind(me.downloadStorageFile, me, [repository.getId(), node.get('path')])
      });
    }
    if (!node.isRoot() && repository.get('type') !== 'group') {
      menu.add({
        text: 'Delete',
        handler: Ext.bind(me.deleteStorageFile, me, [repository, node])
      });
    }
  },

  /**
   * @private
   * Expire repository cache.
   * @param repository repository (model) to expire cache
   * @param path path to expire cache
   */
  expireCache: function(repository, path) {
    NX.direct.coreui_Repository.clearCache(repository.getId(), path, function(response) {
      if (Ext.isDefined(response) && response.success) {
        NX.Messages.add({
          text: 'Started expiring caches of "' + repository.get('name') + '", path "' + path + '"',
          type: 'success'
        });
      }
    });
  },

  /**
   * @private
   * Rebuild Maven metadata.
   * @param repository repository (model) to rebuild metadata
   * @param path path to rebuild metadata
   */
  rebuildMavenMetadata: function(repository, path) {
    NX.direct.coreui_Maven.rebuildMetadata(repository.getId(), path, function(response) {
      if (Ext.isDefined(response) && response.success) {
        NX.Messages.add({
          text: 'Started rebuilding metadata of "' + repository.get('name') + '", path "' + path + '"',
          type: 'success'
        });
      }
    });
  },

  /**
   * @private
   * Download path.
   * @param url to download from
   * @param path to download
   */
  downloadPath: function(url, path) {
    window.open(url + path);
  },

  /**
   * @private
   * Download via content.
   * @param repositoryId id of repository to download from
   * @param path to download
   */
  downloadStorageFile: function(repositoryId, path) {
    this.downloadPath(NX.util.Url.urlOf('content/repositories/' + repositoryId), path);
  },

  /**
   * @private
   * Delete storage file.
   * @param repository repository (model) to delete from
   * @param node to delete
   */
  deleteStorageFile: function(repository, node) {
    var path = node.get('path');

    NX.Dialogs.askConfirmation(
        'Delete Repository Item',
        'Delete the selected "' + path + '" ' + (node.isLeaf() ? 'file' : 'folder'),
        function() {
          NX.direct.coreui_RepositoryStorage.delete(repository.getId(), path, function(response) {
            if (Ext.isDefined(response) && response.success) {
              node.parentNode.removeChild(node);
              NX.Messages.add({
                text: 'Deleted "' + repository.get('name') + '", path "' + path + '"',
                type: 'success'
              });
            }
          });
        }
    );
  }

});