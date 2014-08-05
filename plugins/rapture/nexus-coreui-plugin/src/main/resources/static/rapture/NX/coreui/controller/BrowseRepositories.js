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
 * Browse repositories controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.BrowseRepositories', {
  extend: 'Ext.app.Controller',

  list: 'nx-coreui-repositorybrowse-list',

  stores: [
    'BrowseManagedRepository',
    'BrowseStandardRepository'
  ],
  views: [
    'repositorybrowse.BrowseRepositoryFeature',
    'repositorybrowse.BrowseRepositoryTree',
    'repositorybrowse.StorageFileContainer'
  ],
  refs: [
    { ref: 'tree', selector: 'nx-coreui-repositorybrowse-tree' },
    { ref: 'storageFileContainer', selector: 'nx-coreui-repositorybrowse-storagefilecontainer' }
  ],

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.getApplication().getIconController().addIcons({
      'repository-managed': { file: 'database_yellow.png', variants: ['x16', 'x32'] },
      'repository-out-of-service': { file: 'warning.png', variants: ['x16'] },
      'repositorybrowse-inIndex': { file: 'tick.png', variants: ['x16'] },
      'repositorybrowse-inStorage': { file: 'tick.png', variants: ['x16'] },
      'repository-item-type-default': { file: 'file_extension_default.png', variants: ['x16', 'x24', 'x32'] },
      'repository-item-type-md5': { file: 'file_extension_checksum.png', variants: ['x16', 'x32'] },
      'repository-item-type-jar': { file: 'file_extension_jar.png', variants: ['x16', 'x32'] },
      'repository-item-type-pom': { file: 'file_extension_xml.png', variants: ['x16', 'x32'] },
      'repository-item-type-sha1': { file: 'file_extension_checksum.png', variants: ['x16', 'x32'] },
      'repository-item-type-xml': { file: 'file_extension_xml.png', variants: ['x16', 'x32'] },
      'repository-item-type-zip': { file: 'file_extension_zip.png', variants: ['x16', 'x32'] }
    });

    me.getApplication().getFeaturesController().registerFeature([
      {
        mode: 'browse',
        path: '/Repository/Standard',
        description: 'Browse standard repositories',
        view: { xtype: 'nx-coreui-repositorybrowse-feature', repositoryStore: 'BrowseStandardRepository' },
        weight: 10,
        authenticationRequired: false,
        iconConfig: {
          file: 'database.png',
          variants: ['x16', 'x32']
        },
        visible: function() {
          return NX.Permissions.check('nexus:repositories', 'read');
        }
      },
      {
        mode: 'browse',
        path: '/Repository/Managed',
        description: 'Browse managed repositories',
        view: { xtype: 'nx-coreui-repositorybrowse-feature', repositoryStore: 'BrowseManagedRepository' },
        weight: 300,
        iconConfig: {
          file: 'database_yellow.png',
          variants: ['x16', 'x32']
        },
        visible: function() {
          return NX.Permissions.check('nexus:repositories', 'read');
        }
      }
    ]);

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.loadRepositories
        }
      },
      component: {
        'nx-coreui-repositorybrowse-tree': {
          beforerender: me.loadRepositories,
          select: me.onNodeSelected,
          beforeitemexpand: me.loadChildrenFromIndex,
          beforeitemcontextmenu: me.showContextMenu,
          beforecontextmenushow: me.fillContextMenu
        },
        'nx-coreui-repositorybrowse-list button[action=admin]': {
          afterrender: me.bindAdminButton,
          click: me.navigateToAdminMode
        }
      }
    });
  },

  /**
   * @private
   * Show 'Admin' when user has 'update' permission.
   */
  bindAdminButton: function(button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted('nexus:repositories', 'update')
        ),
        {
          satisfied: button.show,
          unsatisfied: button.hide,
          scope: button
        }
    );
    button.mon(
        NX.Conditions.and(
            NX.Conditions.gridHasSelection('nx-coreui-repositorybrowse-list')
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  /**
   * @private
   * Navigate to same repository in admin mode.
   */
  navigateToAdminMode: function(button) {
    var list = button.up('grid');

    NX.Bookmarks.navigateTo(NX.Bookmarks.fromSegments([
      'admin/repository/repositories', list.getSelectionModel().getSelection()[0].getId()
    ]));
  },

  loadRepositories: function() {
    var me = this,
        tree = me.getTree(),
        store;

    if (tree) {
      store = me.getStore(tree.repositoryStore);
      store.load({
        callback: function() {
          var rootNode = tree.getStore().getRootNode();
          Ext.suspendLayouts();
          rootNode.removeAll();
          store.each(function(model) {
            var inService = model.get('localStatus') === 'IN_SERVICE';
            rootNode.appendChild({
              repositoryId: model.getId(),
              path: '/',
              inIndex: inService,
              inStorage: inService,
              text: model.get('name'),
              qtip: inService ? undefined : 'Out of Service',
              leaf: !inService,
              iconCls: NX.Icons.cls(inService ? 'repository-default' : 'repository-out-of-service', 'x16')
            });
          });
          Ext.resumeLayouts(true);
          me.getStorageFileContainer().showStorageFile(undefined, undefined);
        }
      });
    }
  },

  /**
   * @private
   * When a node gets selected, refresh storage file in right-side container.
   */
  onNodeSelected: function(tree, node) {
    var me = this;
    me.getStorageFileContainer().showStorageFile(
        node.get('repositoryId'),
        (node.isLeaf() && node.get('path') !== '/') ? node.get('path') : undefined
    );
  },

  /**
   * @private
   * Load children of selected node from index, if not already loaded.
   */
  loadChildrenFromIndex: function(node) {
    var me = this,
        tree = me.getTree();

    if (node.get('inIndex') && !node.get('indexLoaded')) {
      node.set('indexLoaded', true);
      tree.getEl().mask('Loading...');
      NX.direct.coreui_BrowseIndex.readChildren(node.get('repositoryId'), node.get('path'), function(response) {
        if (Ext.isObject(response) && response.success && response.data && response.data.length) {
          Ext.suspendLayouts();
          node.removeAll();
          node.appendChild(response.data);
          me.sortNode(node);
          node.cascadeBy(function(child) {
            child.set('inIndex', true);
            if (child.get('processed')) {
              child.set('indexLoaded', true);
            }
          });
          Ext.resumeLayouts(true);
        }
        tree.getEl().unmask();
        me.loadChildrenFromStorage(node);
      });
    }
    else {
      me.loadChildrenFromStorage(node);
    }
  },

  /**
   * @private
   * Load children of selected node from storage, if not already loaded.
   */
  loadChildrenFromStorage: function(node) {
    var me = this,
        tree = me.getTree();

    if (node.get('inStorage') && !node.get('storageLoaded')) {
      node.set('storageLoaded', true);
      tree.getEl().mask('Loading...');
      NX.direct.coreui_RepositoryStorage.readChildren(node.get('repositoryId'), node.get('path'), function(response) {
        if (Ext.isObject(response) && response.success && response.data && response.data.length) {
          Ext.suspendLayouts();
          Ext.Array.each(response.data, function(child) {
            var nodeChild = node.findChild('text', child['text']);
            if (!nodeChild) {
              nodeChild = node.appendChild(child);
            }
            nodeChild.set('inStorage', true);
          });
          me.sortNode(node);
          Ext.resumeLayouts(true);
        }
        tree.getEl().unmask();
      });
    }
  },

  /**
   * @private
   * Sort children by showing dirs first then by name.
   * @param node
   */
  sortNode: function(node) {
    node.sort(function(n1, n2) {
      var t1 = n1.get('text') || '',
          t2 = n2.get('text') || '';

      if (n1.isLeaf() !== n2.isLeaf()) {
        return n1.isLeaf() ? 1 : -1;
      }
      return t1.localeCompare(t2);
    }, true);
  },

  /**
   * @private
   * Show context menu.
   */
  showContextMenu: function(view, node, item, index, event) {
    var me = this,
        tree = me.getTree(),
        menu = Ext.create('Ext.menu.Menu'),
        repository = me.getStore(tree.repositoryStore).getById(node.get('repositoryId'));

    tree.fireEvent('beforecontextmenushow', menu, repository, node);
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
    if ((node.get('path') !== '/') && (repository.get('type') !== 'group')) {
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
      if (Ext.isObject(response) && response.success) {
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
      if (Ext.isObject(response) && response.success) {
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
            if (Ext.isObject(response) && response.success) {
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