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
  mixins: {
    logAware: 'NX.LogAware'
  },

  views: [
    'repository.RepositoryBrowseManagedFeature',
    'repository.RepositoryBrowseManagedList',
    'repository.RepositoryBrowseStandardFeature',
    'repository.RepositoryBrowseStandardList'
  ],
  refs: [
    {
      ref: 'list',
      selector: 'nx-coreui-repository-browse-list'
    }
  ],

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.getApplication().getFeaturesController().registerFeature([
      {
        mode: 'browse',
        path: '/Repository/Standard',
        description: 'Browse standard repositories',
        view: { xtype: 'nx-coreui-repository-browse-standard-feature' },
        weight: 10,
        authenticationRequired: false,
        iconConfig: {
          file: 'database.png',
          variants: ['x16', 'x32']
        },
        visible: function () {
          return NX.Permissions.check('nexus:repositories', 'read');
        }
      },
      {
        mode: 'browse',
        path: '/Repository/Managed',
        description: 'Browse managed repositories',
        view: { xtype: 'nx-coreui-repository-browse-managed-feature' },
        weight: 300,
        iconConfig: {
          file: 'database_yellow.png',
          variants: ['x16', 'x32']
        },
        visible: function () {
          return NX.Permissions.check('nexus:repositories', 'read');
        }
      }
    ]);

    me.listen({
      controller: {
        '#Bookmarking': {
          navigate: me.navigateTo
        },
        '#Refresh': {
          refresh: me.refresh
        }
      },
      component: {
        'nx-coreui-repository-browse-list': {
          beforerender: me.load,
          selectionchange: me.onSelectionChange,
          itemdblclick: me.onItemDblClick
        },
        'nx-coreui-repository-browse-standard-list button[action=admin]': {
          afterrender: me.bindAdminButton,
          click: me.navigateToAdminMode
        }
      }
    });
  },

  refresh: function () {
    var me = this,
        list = me.getList();

    if (list) {
      me.load(list);
    }
  },

  load: function (list) {
    var me = this;

    if (!list.repositoryStore) {
      list.repositoryStore = Ext.create('NX.coreui.store.Repository', { remoteFilter: true });
      list.repositoryStore.filter({ property: 'includeNexusManaged', value: 'true' });
    }
    else {
      list.repositoryStore.filter();
    }
    list.getStore().on('load', function () {
      me.navigateTo(NX.Bookmarks.getBookmark());
    }, me, { single: true });
    list.getStore().load();
  },

  onSelectionChange: function (selectionModel, selected) {
    var me = this,
        list = me.getList(),
        bookmark = NX.Bookmarks.fromToken(NX.Bookmarks.getBookmark().getSegment(0)),
        repositoryModel;

    if (selected.length) {
      repositoryModel = list.repositoryStore.getById(selected[0].getId());
      list.setTitle(repositoryModel ? repositoryModel.get('name') : '');
      if (repositoryModel) {
        list.fireEvent('selection', list, repositoryModel);
      }
      bookmark.appendSegments(encodeURIComponent(selected[0].getId()));
    }
    NX.Bookmarks.bookmark(bookmark, me);
  },

  onItemDblClick: function () {
    var me = this,
        list = me.getList();

    list.collapse();
  },

  /**
   * @private
   * @param {NX.Bookmark} bookmark to navigate to
   */
  navigateTo: function (bookmark) {
    var me = this,
        list = me.getList(),
        store, modelId, model;

    if (list && bookmark) {
      modelId = bookmark.getSegment(1);
      if (modelId) {
        modelId = decodeURIComponent(modelId);
        me.logDebug('Navigate to: ' + modelId);
        store = list.getStore();
        model = store.getById(modelId);
        if (model) {
          list.getSelectionModel().select(model, false, false);
          list.getView().focusRow(model);
        }
      }
      else {
        list.getSelectionModel().deselectAll();
      }
    }
  },

  /**
   * @protected
   * Show 'Admin' when user has 'update' permission.
   */
  bindAdminButton: function (button) {
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
            NX.Conditions.gridHasSelection('nx-coreui-repository-browse-list')
        ),
        {
          satisfied: button.enable,
          unsatisfied: button.disable,
          scope: button
        }
    );
  },

  navigateToAdminMode: function (button) {
    var list = button.up('grid');

    NX.Bookmarks.navigateTo(NX.Bookmarks.fromSegments([
      'admin/repository/repositories', list.getSelectionModel().getSelection()[0].getId()
    ]));
  }

});