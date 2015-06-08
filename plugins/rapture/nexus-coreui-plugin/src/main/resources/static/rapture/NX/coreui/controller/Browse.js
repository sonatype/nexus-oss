/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2008-2015 Sonatype, Inc.
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

  masters: ['nx-coreui-browse-repository-list', 'nx-coreui-browse-result-list', 'nx-coreui-component-asset-list'],

  stores: [
    'Asset',
    'BrowseResult'
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
    {ref: 'assets', selector: 'nx-coreui-browsefeature nx-coreui-component-asset-list'}
  ],

  features: {
    mode: 'browse',
    path: '/Browse',
    text: NX.I18n.get('Coreui_Controller_Browse_Title_Feature'),
    description: NX.I18n.get('Coreui_Controller_Browse_Description_Feature'),
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
      component: {
        'nx-coreui-browsefeature nx-coreui-component-assetcontainer': {
          updated: me.setAssetIcon
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
        browseResultStore = me.getBrowseResultStore(),
        results = me.getResults();

    results.getSelectionModel().deselectAll();
    browseResultStore.addFilter([
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
   * Set the appropriate breadcrumb icon.
   * @param {NX.coreui.model.Component} componentModel selected asset
   * @param {NX.coreui.model.Asset} assetModel selected asset
   */
  setAssetIcon: function(container, componentModel, assetModel) {
    var me = this,
        feature = me.getFeature();

    if (assetModel) {
      // Set the appropriate breadcrumb icon
      feature.setItemClass(3, container.iconCls);
    }
  }

});
