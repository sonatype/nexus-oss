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
/*global Ext, NX*/

/**
 * Browse repositories controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.BrowseRepositories', {
  extend: 'Ext.app.Controller',
  requires: [
    'NX.Bookmarks',
    'NX.Windows'
  ],
  mixins: {
    logAware: 'NX.LogAware'
  },

  list: 'nx-coreui-repositorybrowse-list',

  stores: [
    'BrowseManagedRepository',
    'BrowseStandardRepository'
  ],
  views: [
    'repositorybrowse.BrowseRepositoryFeature',
    'repositorybrowse.BrowseRepositoryTree'
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
      'repositorybrowse-inStorage': { file: 'tick.png', variants: ['x16'] }
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
        },
        '#Bookmarking': {
          navigate: me.navigateTo
        }
      },
      component: {
        'nx-coreui-repositorybrowse-tree': {
          beforerender: me.loadRepositories,
          select: me.onNodeSelected,
          beforeitemexpand: me.loadChildrenFromIndex
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

  /**
   * Load available repositories into tree.
   */
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
          me.getStorageFileContainer().showStorageFile();
          me.navigateTo(NX.Bookmarks.getBookmark());
        }
      });
    }
  },

  /**
   * @private
   * When a node gets selected, refresh storage file in right-side container and bookmark.
   */
  onNodeSelected: function(tree, node) {
    var me = this,
        bookmark = NX.Bookmarks.fromToken(NX.Bookmarks.getBookmark().getSegment(0));

    me.showStorageFile(tree, node);
    bookmark.appendSegments([
      encodeURIComponent(node.get('repositoryId')),
      encodeURIComponent(node.get('path')).replace(/%2F/g, '/')
    ]);
    NX.Bookmarks.bookmark(bookmark, me);
  },

  /**
   * @private
   * When a node gets selected, refresh storage file in right-side container.
   */
  showStorageFile: function(tree, node) {
    var me = this,
        treePanel = me.getTree(),
        moreButton = treePanel.down('button[action=more]'),
        repositoryModel = me.getStore(treePanel.repositoryStore).getById(node.get('repositoryId'));

    moreButton.enable();
    me.fillMoreButtonMenu(moreButton, repositoryModel, node);
    if (moreButton.menu.items.length === 0) {
      moreButton.disable();
    }
    me.getStorageFileContainer().showStorageFile(
        node.get('repositoryId'),
        (node.isLeaf() && node.get('path') !== '/') ? node.get('path') : undefined,
        (node.isLeaf() && node.get('path') !== '/') ? node.get('type') : undefined
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
        if (node.expandFn) {
          node.expandFn(node);
          delete node.expandFn;
        }
        tree.getEl().unmask();
      });
    }
    else {
      if (node.expandFn) {
        node.expandFn(node);
        delete node.expandFn;
      }
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
   * @param {NX.Bookmark} bookmark to navigate to
   */
  navigateTo: function(bookmark) {
    var me = this,
        tree = me.getTree(),
        store, repositoryId, path, node;

    if (tree && bookmark) {
      repositoryId = bookmark.getSegment(1);
      path = bookmark.getSegment(2);
      tree.collapseAll(function() {
        if (repositoryId) {
          repositoryId = decodeURIComponent(repositoryId);
          me.logDebug('Navigate to: ' + repositoryId + path);
          store = tree.getStore();
          node = store.getRootNode().findChild('repositoryId', repositoryId);
          if (node) {
            tree.getSelectionModel().select(node, false, true);
            tree.getView().focusRow(node);
            if (path) {
              node.expandFn = Ext.bind(me.expandNode, me, [path], true);
              node.expand();
            }
          }
        }
        else {
          tree.getSelectionModel().deselectAll();
        }
      });
    }
  },

  /**
   * @private
   * Expand & select a child path.
   * @param node containing the child path
   * @param childPath child path to expand
   */
  expandNode: function(node, childPath) {
    var me = this,
        tree = me.getTree(),
        childNode, childName, segments;

    if (childPath > '/') {
      segments = childPath.substring(1).split('/');
      childName = segments[0];
      if (segments.length > 1) {
        childName += '/';
      }
      childNode = node.findChild('path', node.get('path') + childName);
      if (childNode) {
        tree.getSelectionModel().select(childNode, false, true);
        tree.getView().focusRow(childNode);
        me.showStorageFile(tree, childNode);
        if (segments.length > 1) {
          childNode.expandFn = Ext.bind(me.expandNode, me, [childPath.substring(childName.length)], true);
          childNode.expand();
        }
      }
    }
  },

  /**
   * @private
   * Add menu entries to 'More' button, based on current selected node.
   * @param {Ext.button.Button} button 'More' button
   * @param {NX.coreui.model.Repository} repository repository model
   * @param {Ext.data.NodeInterface} node selected node
   */
  fillMoreButtonMenu: function(button, repository, node) {
    var me = this,
        menu = button.menu;

    me.removeMenuItem(button, 'expirecache');
    if (NX.Permissions.check('nexus:cache', 'delete')
        && repository.get('type') !== 'virtual' && repository.get('userManaged')) {
      menu.add({
        text: 'Expire Cache', action: 'expirecache',
        handler: Ext.bind(me.expireCache, me, [repository, node.get('path')])
      });
    }

    me.removeMenuItem(button, 'rebuildmetadata');
    if (NX.Permissions.check('nexus:metadata', 'delete')
        && (repository.get('format') === 'maven1' || repository.get('format') === 'maven2' )
        && (repository.get('type') === 'hosted' || repository.get('type') === 'group' )
        && repository.get('userManaged')) {
      menu.add({
        text: 'Rebuild Metadata', action: 'rebuildmetadata',
        handler: Ext.bind(me.rebuildMavenMetadata, me, [repository, node.get('path')])
      });
    }

    me.removeMenuItem(button, 'downloadfromremote');
    if (node.isLeaf() && repository.get('type') === 'proxy') {
      menu.add({
        text: 'Download From Remote', action: 'downloadfromremote',
        handler: Ext.bind(me.downloadPath, me, [repository.get('remoteStorageUrl'), node.get('path')])
      });
    }

    me.removeMenuItem(button, 'viewremote');
    if (!node.isRoot() && !node.isLeaf() && repository.get('type') === 'proxy') {
      menu.add({
        text: 'View Remote', action: 'viewremote',
        handler: Ext.bind(me.downloadPath, me, [repository.get('remoteStorageUrl'), node.get('path')])
      });
    }

    me.removeMenuItem(button, 'download');
    if (node.isLeaf()) {
      menu.add({
        text: 'Download', action: 'download',
        handler: Ext.bind(me.downloadStorageFile, me, [repository.getId(), node.get('path')])
      });
    }

    me.removeMenuItem(button, 'delete');
    if ((node.get('path') !== '/') && (repository.get('type') !== 'group')) {
      menu.add({
        text: 'Delete', action: 'delete',
        handler: Ext.bind(me.deleteStorageFile, me, [repository, node])
      });
    }
  },

  /**
   * @private
   * Removes from 'More' button, if present, menu item with specified action.
   * @param {Ext.button.Button} button 'More' button
   * @param actionName menu item action name to be removed
   */
  removeMenuItem: function(button, actionName) {
    var menuItem = button.down('menuitem[action=' + actionName + ']');
    if (menuItem) {
      button.menu.remove(menuItem);
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
    NX.Windows.open(url + path);
  },

  /**
   * @private
   * Download via content.
   * @param repositoryId id of repository to download from
   * @param path to download
   */
  downloadStorageFile: function(repositoryId, path) {
    var me = this,
        repositoryNode, itemNode,
        setInStorage = function(nodeToSet) {
          if (!nodeToSet.get('inStorage')) {
            nodeToSet.set('inStorage', true);
            if (nodeToSet.parentNode && !(nodeToSet.get('path') === '/')) {
              setInStorage(nodeToSet.parentNode);
            }
          }
        };

    me.downloadPath(NX.util.Url.urlOf('content/repositories/' + repositoryId), path);
    repositoryNode = me.getTree().getRootNode().findChild('repositoryId', repositoryId);
    if (repositoryNode) {
      itemNode = repositoryNode.findChild('path', path, true);
      if (itemNode) {
        setInStorage(itemNode);
      }
    }
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
          NX.direct.coreui_RepositoryStorage.delete_(repository.getId(), path, function(response) {
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