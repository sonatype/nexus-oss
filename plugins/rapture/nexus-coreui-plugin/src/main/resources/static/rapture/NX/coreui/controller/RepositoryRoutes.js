/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2013 Sonatype, Inc.
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
 * Repository Routes controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.RepositoryRoutes', {
  extend: 'NX.controller.MasterDetail',
  requires: [
    'NX.Dialogs',
  ],

  list: 'nx-coreui-repositoryroute-list',

  models: [
    'RepositoryRoute'
  ],
  stores: [
    'RepositoryRoute',
    'Repository'
  ],
  views: [
    'repositoryroute.RepositoryRouteAdd',
    'repositoryroute.RepositoryRouteFeature',
    'repositoryroute.RepositoryRouteList',
    'repositoryroute.RepositoryRouteSettings'
  ],
  refs: [
    {
      ref: 'list',
      selector: 'nx-coreui-repositoryroute-list'
    },
    {
      ref: 'settings',
      selector: 'nx-coreui-repositoryroute-feature nx-coreui-repositoryroute-settings'
    }
  ],
  icons: {
    'feature-repository-routing': {
      file: 'arrow_branch.png',
      variants: ['x16', 'x32']
    },
    'repositoryroute-default': {
      file: 'arrow_branch.png',
      variants: ['x16', 'x32']
    }
  },
  features: {
    path: '/Repository/Routing',
    view: { xtype: 'nx-coreui-repositoryroute-feature' },
    visible: function () {
      return NX.Permissions.check('nexus:routes', 'read');
    }
  },
  permission: 'nexus:routes',

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.callParent();

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.onRefresh
        }
      },
      component: {
        'nx-coreui-repositoryroute-list button[action=new]': {
          click: me.showAddWindow
        },
        'nx-coreui-repositoryroute-settings': {
          submitted: me.onSettingsSubmitted
        },
        'nx-coreui-repositoryroute-settings #mappingType': {
          change: me.onMappingTypeChanged
        }
      }
    });
  },

  /**
   * @override
   */
  getDescription: function (model) {
    return model.get('pattern');
  },

  /**
   * @override
   */
  onSelection: function (list, model) {
    var me = this;

    if (Ext.isDefined(model)) {
      me.getSettings().loadRecord(model);
    }
  },

  /**
   * @private
   */
  showAddWindow: function () {
    Ext.widget('nx-coreui-repositoryroute-add');
  },

  /**
   * @private
   * Reload stores used in settings.
   */
  onRefresh: function () {
    var me = this,
        settings = me.getSettings();

    if (settings) {
      settings.down('#groupId').getStore().load();
      settings.down('#mappedRepositoriesIds').getStore().load();
    }
  },

  /**
   * @private
   * Hides mapped repositories if mapping type = 'BLOCKING'.
   */
  onMappingTypeChanged: function (mappingTypeCombo, newValue) {
    var mappedRepositories = mappingTypeCombo.up('form').down('#mappedRepositoriesIds');

    if (newValue === 'BLOCKING') {
      mappedRepositories.hide();
      mappedRepositories.setValue(undefined);
    }
    else {
      mappedRepositories.show();
    }
  },

  /**
   * @private
   */
  onSettingsSubmitted: function (form, action) {
    var me = this,
        win = form.up('nx-coreui-repositoryroute-add');

    if (win) {
      win.close();
      me.loadStoreAndSelect(action.result.data.id);
    }
    else {
      me.loadStore();
    }
  },

  /**
   * @private
   * @override
   * Deletes a repository route.
   * @param {NX.coreui.model.RepositoryRoute} model repository route to be deleted
   */
  deleteModel: function (model) {
    var me = this,
        description = me.getDescription(model);

    NX.direct.coreui_RepositoryRoute.delete(model.getId(), function (response) {
      me.loadStore();
      if (Ext.isDefined(response) && response.success) {
        NX.Messages.add({ text: 'Route deleted: ' + description, type: 'success' });
      }
    });
  }

});