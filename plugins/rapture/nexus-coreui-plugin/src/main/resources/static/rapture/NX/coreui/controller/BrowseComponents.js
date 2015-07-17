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
 * Browse components controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.BrowseComponents', {
  extend: 'NX.controller.Drilldown',
  requires: [
    'NX.Bookmarks',
    'NX.Conditions',
    'NX.Permissions',
    'NX.I18n'
  ],
  masters: [
    'nx-coreui-browsecomponentfeature nx-coreui-browse-repository-list',
    'nx-coreui-browsecomponentfeature nx-coreui-browse-component-list',
    'nx-coreui-browsecomponentfeature nx-coreui-component-asset-list'
  ],
  stores: [
    'Component',
    'ComponentAsset'
  ],
  models: [
    'Component'
  ],

  views: [
    'browse.BrowseComponentFeature',
    'browse.BrowseComponentList',
    'browse.BrowseRepositoryList'
  ],

  refs: [
    {ref: 'feature', selector: 'nx-coreui-browsecomponentfeature'},
    {ref: 'repositoryList', selector: 'nx-coreui-browsecomponentfeature nx-coreui-browse-repository-list'},
    {ref: 'componentList', selector: 'nx-coreui-browsecomponentfeature nx-coreui-browse-component-list'},
    {ref: 'assetList', selector: 'nx-coreui-browsecomponentfeature nx-coreui-component-asset-list'},
    {ref: 'componentDetails', selector: 'nx-coreui-browsecomponentfeature nx-coreui-component-details'},
    {ref: 'componentFilter', selector: 'nx-coreui-browsecomponentfeature nx-coreui-browse-component-list #filter'}
  ],

  features: {
    mode: 'browse',
    path: '/Browse/Components',
    text: NX.I18n.get('Browse_Components_Title_Feature'),
    description: NX.I18n.get('Browse_Components_Description_Feature'),
    view: 'NX.coreui.view.browse.BrowseComponentFeature',
    iconConfig: {
      file: 'plugin.png',
      variants: ['x16', 'x32']
    },
    visible: function() {
      return NX.Permissions.checkExistsWithPrefix('nexus:repository-view');
    }
  },

  icons: {
    'browse-component-default': {file: 'database.png', variants: ['x16', 'x32']},
    'browse-component': {file: 'box_front.png', variants: ['x16', 'x32']},
    'browse-component-detail': {file: 'box_front_open.png', variants: ['x16', 'x32']}
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
      store: {
        '#Repository': {
          load: me.reselect
        }
      },
      component: {
        'nx-coreui-browsecomponentfeature nx-coreui-browse-component-list #filter': {
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
    else if (modelType === "Component") {
      me.onComponentSelection(model);
    }
  },

  /**
   * @private
   *
   * Load browse results for selected repository.
   *
   * @param {NX.coreui.model.Repository} model selected repository
   */
  onRepositorySelection: function(model) {
    var me = this,
        componentStore = me.getStore('Component'),
        componentList = me.getComponentList(),
        componentFilter = me.getComponentFilter();

    componentStore.filters.removeAtKey('filter');
    componentFilter.clearSearch();
    componentList.getSelectionModel().deselectAll();
    componentStore.addFilter([
      {
        id: 'repositoryName',
        property: 'repositoryName',
        value: model.get('name')
      }
    ]);
  },

  /**
   * @private
   *
   * Show component details and load assets for selected component.
   *
   * @param {NX.coreui.model.Component} model selected component
   */
  onComponentSelection: function(model) {
    var me = this;

    me.getComponentDetails().setComponentModel(model);
    me.getAssetList().setComponentModel(model);
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
        this.getComponentList().getStore().load();
      }
      if (this.currentIndex === 2) {
        this.getAssetList().getStore().load();
      }
    }
  },

  /**
   * @private
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
  }

});
