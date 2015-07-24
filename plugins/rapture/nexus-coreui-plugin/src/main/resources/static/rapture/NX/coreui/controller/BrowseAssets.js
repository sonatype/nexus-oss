/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-present Sonatype, Inc.
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
 * Browse controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.BrowseAssets', {
  extend: 'NX.controller.Drilldown',
  requires: [
    'NX.Bookmarks',
    'NX.Conditions',
    'NX.Permissions',
    'NX.I18n',
    'NX.coreui.controller.Assets'
  ],
  masters: [
    'nx-coreui-browseassetfeature nx-coreui-browse-repository-list',
    'nx-coreui-browseassetfeature nx-coreui-browse-asset-list'
  ],
  stores: [
    'Repository',
    'Asset'
  ],
  models: [
    'Asset'
  ],

  views: [
    'browse.BrowseAssetFeature',
    'browse.BrowseAssetList',
    'browse.BrowseRepositoryList'
  ],

  refs: [
    {ref: 'feature', selector: 'nx-coreui-browseassetfeature'},
    {ref: 'repositoryList', selector: 'nx-coreui-browseassetfeature nx-coreui-browse-repository-list'},
    {ref: 'assetList', selector: 'nx-coreui-browseassetfeature nx-coreui-browse-asset-list'},
    {ref: 'assetFilter', selector: 'nx-coreui-browseassetfeature nx-coreui-browse-asset-list #filter'},
    {ref: 'assetContainer', selector: 'nx-coreui-browseassetfeature nx-coreui-component-assetcontainer'}
  ],

  features: {
    mode: 'browse',
    path: '/Browse/Assets',
    text: NX.I18n.get('Browse_Assets_Title_Feature'),
    description: NX.I18n.get('Browse_Assets_Description_Feature'),
    view: 'NX.coreui.view.browse.BrowseAssetFeature',
    iconConfig: {
      file: 'page_white_stack.png',
      variants: ['x16', 'x32']
    },
    visible: function() {
      return NX.Permissions.checkExistsWithPrefix('nexus:repository-view');
    },
    authenticationRequired: false
  },

  icons: {
    'browse-asset-default': {
      file: 'page_white.png',
      variants: ['x16', 'x32']
    }
  },

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.callParent();

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.loadStores
        }
      },
      component: {
        'nx-coreui-browseassetfeature nx-coreui-browse-repository-list': {
          beforerender: me.onBeforeRender
        },
        'nx-coreui-browseassetfeature nx-coreui-component-assetcontainer': {
          updated: me.setAssetIcon
        },
        'nx-coreui-browseassetfeature nx-coreui-browse-asset-list #filter': {
          search: me.onSearch,
          searchcleared: me.onSearchCleared
        }
      }
    });
  },

  /**
   * @override
   */
  getDescription: function(model) {
    return model.get('name');
  },

  /**
   * @override
   * When a list managed by this controller is clicked, route the event to the proper handler
   */
  onSelection: function(list, model) {
    var me = this,
        modelType;

    // Figure out what kind of list we’re dealing with
    modelType = model.id.replace(/^.*?model\./, '').replace(/\-.*$/, '');

    if (modelType === "RepositoryReference") {
      me.onRepositorySelection(model);
    }
    else if (modelType === "Asset") {
      me.onAssetSelection(model);
    }
  },

  /**
   * Load assets for selected repository.
   *
   * @private
   * @param {NX.coreui.model.RepositoryReference} model selected repository
   */
  onRepositorySelection: function(model) {
    var me = this,
        assetStore = me.getStore('Asset'),
        assetList = me.getAssetList(),
        assetFilter = me.getAssetFilter();

    assetStore.filters.removeAtKey('filter');
    assetFilter.clearSearch();
    assetList.getSelectionModel().deselectAll();
    assetStore.addFilter([
      {
        id: 'repositoryName',
        property: 'repositoryName',
        value: model.get('name')
      }
    ]);
  },

  /**
   * Load asset container for selected asset
   *
   * @private
   * @param {NX.coreui.model.Asset} model selected asset
   */
  onAssetSelection: function(model) {
    this.getController('NX.coreui.controller.Assets').updateAssetContainer(null, null, null, model);
  },

  /**
   * @override
   * Load all of the stores associated with this controller.
   */
  loadStores: function() {
    if (this.getFeature()) {
      if (this.currentIndex === 0) {
        this.getRepositoryList().getStore().load();
      }
      if (this.currentIndex === 1) {
        this.getAssetList().getStore().load();
      }
    }
  },

  /**
   * @private
   * Load stores based on the bookmarked URL
   */
  onBeforeRender: function() {
    var me = this,
      bookmark = NX.Bookmarks.getBookmark(),
      list_ids = bookmark.getSegments().slice(1),
      repoStore = me.getRepositoryList().getStore(),
      repoModel;

    repoStore.load(function() {
      // Load the asset detail view
      if (list_ids[1]) {
        repoModel = repoStore.getById(decodeURIComponent(list_ids[0]));
        me.onModelChanged(0, repoModel);
        me.onRepositorySelection(repoModel);
        me.getAssetList().getStore().load(function() {
          me.reselect();
        });
      }
      // Load the asset list view
      else if (list_ids[0]) {
        me.reselect();
      }
    });
  },

  /**
   * Filter grid.
   *
   * @private
   * @param {NX.ext.SearchBox} searchBox component
   * @param {String} value to be searched
   */
  onSearch: function(searchBox, value) {
    var grid = searchBox.up('grid'),
        store = grid.getStore(),
        emptyText = grid.getView().emptyTextFilter;

    if (!grid.emptyText) {
      grid.emptyText = grid.getView().emptyText;
    }
    grid.getView().emptyText = '<div class="x-grid-empty">' + emptyText.replace(/\$filter/, value) + '</div>';
    grid.getSelectionModel().deselectAll();
    store.addFilter([
      {
        id: 'filter',
        property: 'filter',
        value: value
      }
    ]);
  },

  /**
   * Clear filtering on grid.
   *
   * @private
   * @param {NX.ext.SearchBox} searchBox component
   */
  onSearchCleared: function(searchBox) {
    var grid = searchBox.up('grid'),
        store = grid.getStore();

    if (grid.emptyText) {
      grid.getView().emptyText = grid.emptyText;
    }
    grid.getSelectionModel().deselectAll();
    // we have to remove filter directly as store#removeFilter() does not work when store#remoteFilter = true
    if (store.filters.removeAtKey('filter')) {
      if (store.filters.length) {
        store.filter();
      }
      else {
        store.clearFilter();
      }
    }
  },

  /**
   * Set the appropriate breadcrumb icon.
   *
   * @private
   * @param {NX.coreui.view.component.AssetContainer} container asset container
   * @param {NX.coreui.model.Asset} assetModel selected asset
   */
  setAssetIcon: function(container, assetModel) {
    if (assetModel) {
      // Set the appropriate breadcrumb icon
      this.setItemClass(2, container.iconCls);
    }
  }

});
