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
 * Repositories CMA controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.RepositoriesCMA', {
  extend: 'NX.controller.Drilldown',
  requires: [
    'NX.Dialogs',
    'NX.Messages',
    'NX.Permissions',
    'NX.I18n'
  ],

  masters: 'nx-coreui-repositorycma-list',

  models: [
    'RepositoryCMA'
  ],
  stores: [
    'RepositoryCMA',
    'RepositoryCMARecipe'
  ],
  views: [
    'repositorycma.RepositoryCMAAdd',
    'repositorycma.RepositoryCMAFeature',
    'repositorycma.RepositoryCMAList',
    'repositorycma.RepositoryCMASettings',
    'repositorycma.RepositoryCMASettingsForm',
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-repositorycma-feature' },
    { ref: 'list', selector: 'nx-coreui-repositorycma-list' },
    { ref: 'settings', selector: 'nx-coreui-repositorycma-feature nx-coreui-repositorycma-settings' }
  ],
  icons: {
    'repository-default': {
      file: 'database_green.png',
      variants: ['x16', 'x32']
    }
  },
  features: {
    mode: 'admin',
    path: '/Repository/Repositories CMA',
    text: NX.I18n.get('ADMIN_REPOSITORIES_TITLE') + " CMA",
    description: NX.I18n.get('ADMIN_REPOSITORIES_SUBTITLE') + " CMA",
    view: { xtype: 'nx-coreui-repositorycma-feature' },
    iconConfig: {
      file: 'database_green.png',
      variants: ['x16', 'x32']
    },
    visible: function() {
      return NX.Permissions.check('nexus:repositories', 'read');
    }
  },
  permission: 'nexus:repositories',

  /**
   * @override
   */
  init: function() {
    var me = this;

    me.callParent();

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.loadRecipe
        }
      },
      component: {
        'nx-coreui-repositorycma-list': {
          beforerender: me.loadRecipe
        },
        'nx-coreui-repositorycma-list button[action=new]': {
          click: me.showAddWindow
        },
        'nx-coreui-repositorycma-settings-form': {
          submitted: me.onSettingsSubmitted
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
   */
  onSelection: function(list, model) {
    var me = this;

    if (Ext.isDefined(model)) {
      me.getSettings().loadRecord(model);
    }
  },

  /**
   * @private
   */
  showAddWindow: function() {
    var me = this,
        feature = me.getFeature();

    // Show the first panel in the create wizard, and set the breadcrumb
    feature.setItemName(1, NX.I18n.get('ADMIN_REPOSITORIES_CMA_CREATE_TITLE'));
    me.loadCreateWizard(1, true, null);
  },

  /**
   * @private
   */
  loadRecipe: function() {
    var me = this,
        list = me.getList();

    if (list) {
      me.getRepositoryCMARecipeStore().load();
    }
  },

  /**
   * @private
   */
  onSettingsSubmitted: function(form, action) {
    var me = this,
        win = form.up('nx-coreui-repositorycma-add');

    if (win) {
      me.loadStoreAndSelect(action.result.data.id, false);
    }
    else {
      me.loadStore(Ext.emptyFn);
    }
  },

  /**
   * @private
   */
  deleteModel: function(model) {
    var me = this,
        description = me.getDescription(model);

    NX.direct.coreui_RepositoryCMA.remove(model.getId(), function(response) {
      me.loadStore(Ext.emptyFn);
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({ text: 'Repository deleted: ' + description, type: 'success' });
      }
    });
  }

});
