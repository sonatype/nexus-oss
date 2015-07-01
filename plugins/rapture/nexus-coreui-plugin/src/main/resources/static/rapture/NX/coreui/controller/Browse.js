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
Ext.define('NX.coreui.controller.Browse', {
  extend: 'NX.controller.Drilldown',
  requires: [
    'NX.Bookmarks',
    'NX.Conditions',
    'NX.Permissions',
    'NX.I18n'
  ],
  masters: [
    'nx-coreui-browse-repository-list',
    'nx-coreui-browse-result-list',
    'nx-coreui-component-asset-list'
  ],
  stores: [
    'Asset',
    'Component'
  ],
  models: [
    'Component'
  ],

  views: [
    'browse.BrowseFeature',
    'browse.BrowseRepositoryList',
    'browse.BrowseResultList'
  ],

  refs: [
    {ref: 'feature', selector: 'nx-coreui-browsefeature'},
    {ref: 'results', selector: 'nx-coreui-browsefeature nx-coreui-browse-result-list'},
    {ref: 'componentDetails', selector: 'nx-coreui-browsefeature nx-coreui-component-details'},
    {ref: 'assets', selector: 'nx-coreui-browsefeature nx-coreui-component-asset-list'},
    {ref: 'componentFilter', selector: 'nx-coreui-browsefeature nx-coreui-browse-result-list #filter'}
  ],

  features: {
    mode: 'browse',
    path: '/Browse',
    text: NX.I18n.get('Browse_Title_Feature'),
    description: NX.I18n.get('Browse_Description_Feature'),
    view: 'NX.coreui.view.browse.BrowseFeature',
    iconConfig: {
      file: 'plugin.png',
      variants: ['x16', 'x32']
    },
    visible: function() {
      return NX.Permissions.check('nexus:search:read');
    }
  },

  icons: {
    'browse-default': {file: 'database.png', variants: ['x16', 'x32']},
    'browse-component': {file: 'box_front.png', variants: ['x16', 'x32']},
    'browse-component-detail': {file: 'box_front_open.png', variants: ['x16', 'x32']},
    'browse-unattached': {file: 'file_extension_default.png', variants: ['x16', 'x32']}
  },

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.callParent();

    me.listen({
      store: {
        '#Repository': {
          load: me.reselect
        }
      },
      component: {
        'nx-coreui-browsefeature nx-coreui-component-assetcontainer': {
          updated: me.setAssetIcon
        },
        'nx-coreui-browsefeature nx-coreui-browse-result-list #filter': {
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

    if (modelType == "Repository") {
      me.onRepositorySelection(model);
    }
    else if (modelType == "Component") {
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
        results = me.getResults(),
        componentFilter = me.getComponentFilter();

    componentStore.filters.removeAtKey('filter');
    componentFilter.clearSearch();
    results.getSelectionModel().deselectAll();
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
    me.getAssets().setComponentModel(model);
  },

  /**
   * @private
   * Filter grid.
   *
   * @param {NX.ext.SearchBox} searchBox component
   * @param {String} value to be searched
   */
  onSearch: function(searchBox, value) {
    var me = this,
        componentStore = me.getStore('Component'),
        results = me.getResults(),
        emptyText = NX.I18n.get('Browse_BrowseResultList_EmptyText_Filter');

    if (!results.emptyText) {
      results.emptyText = results.getView().emptyText;
    }
    results.getView().emptyText = '<div class="x-grid-empty">' + emptyText.replace(/\$filter/, value) + '</div>';
    results.getSelectionModel().deselectAll();
    componentStore.addFilter([
      {
        id: 'filter',
        property: 'filter',
        value: value
      }
    ]);
  },

  /**
   * @private
   * Clear filtering on grid.
   */
  onSearchCleared: function() {
    var me = this,
        componentStore = me.getStore('Component'),
        results = me.getResults();

    if (results.emptyText) {
      results.getView().emptyText = results.emptyText;
    }
    results.getSelectionModel().deselectAll();
    // we have to remove filter directly as store#removeFilter() does not work when store#remoteFilter = true
    if (componentStore.filters.removeAtKey('filter')) {
      if (componentStore.filters.length) {
        componentStore.filter();
      }
      else {
        componentStore.clearFilter();
      }
    }
  },

  /**
   * @private
   * Set the appropriate breadcrumb icon.
   * @param {NX.coreui.model.Component} componentModel selected asset
   * @param {NX.coreui.model.Asset} assetModel selected asset
   */
  setAssetIcon: function(container, componentModel, assetModel) {
    if (assetModel) {
      // Set the appropriate breadcrumb icon
      this.getFeature().setItemClass(3, container.iconCls);
    }
  }

});
