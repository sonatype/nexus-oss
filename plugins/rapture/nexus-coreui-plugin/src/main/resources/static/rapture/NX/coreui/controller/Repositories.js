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
 * Repositories controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Repositories', {
  extend: 'NX.controller.Drilldown',
  requires: [
    'NX.Dialogs',
    'NX.Messages',
    'NX.Permissions',
    'NX.I18n'
  ],

  masters: 'nx-coreui-repository-list',

  models: [
    'NX.coreui.store.Repository'
  ],
  stores: [
    'NX.coreui.store.Repository',
    'RepositoryRecipe'
  ],
  views: [
    'repository.RepositoryAdd',
    'repository.RepositoryFeature',
    'repository.RepositoryList',
    'repository.RepositorySettings',
    'repository.RepositorySettingsForm'
  ],
  refs: [
    { ref: 'feature', selector: 'nx-coreui-repository-feature' },
    { ref: 'list', selector: 'nx-coreui-repository-list' },
    { ref: 'settings', selector: 'nx-coreui-repository-feature nx-coreui-repository-settings' }
  ],
  icons: {
    'repository-hosted': {
      file: 'database_green.png',
      variants: ['x16', 'x32']
    },
    'repository-proxy': {
      file: 'database_link.png',
      variants: ['x16', 'x32']
    },
    'repository-group': {
      file: 'folder_database.png',
      variants: ['x16', 'x32']
    }
  },
  features: {
    mode: 'admin',
    path: '/Repository/Repositories',
    text: NX.I18n.get('ADMIN_REPOSITORIES_TITLE'),
    description: NX.I18n.get('ADMIN_REPOSITORIES_SUBTITLE'),
    view: { xtype: 'nx-coreui-repository-feature' },
    iconConfig: {
      file: 'database_green.png',
      variants: ['x16', 'x32']
    },
    visible: function() {
      return NX.Permissions.check('nexus:repositories', 'read') && NX.State.getUser();
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
        },
        '#State': {
          receivingchanged: me.onStateReceivingChanged
        }
      },
      component: {
        'nx-coreui-repository-list': {
          beforerender: me.loadRecipe,
          afterrender: me.startStatusPolling,
          beforedestroy: me.stopStatusPolling
        },
        'nx-coreui-repository-list button[action=new]': {
          click: me.showAddWindow
        },
        'nx-coreui-repository-settings-form': {
          submitted: me.onSettingsSubmitted
        },
        'nx-coreui-repository-settings button[action=save]': {
          click: me.updateRepository
        },
        'nx-coreui-repository-add button[action=add]': {
          click: me.createRepository
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
    feature.setItemName(1, NX.I18n.get('ADMIN_REPOSITORIES_CREATE_TITLE'));
    me.loadCreateWizard(1, true, Ext.create('widget.nx-coreui-repository-add'));
  },

  /**
   * @private
   */
  loadRecipe: function() {
    var me = this,
        list = me.getList();

    if (list) {
      me.getRepositoryRecipeStore().load();
    }
  },

  /**
   * @private
   */
  onSettingsSubmitted: function(form, action) {
    var me = this,
        win = form.up('nx-coreui-repository-add');

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

    NX.direct.coreui_Repository.remove(model.getId(), function(response) {
      me.loadStore(Ext.emptyFn);
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({ text: 'Repository deleted: ' + description, type: 'success' });
      }
    });
  },

  /**
   * @private
   * Start polling for repository statuses.
   */
  startStatusPolling: function() {
    var me = this;

    if (me.statusProvider) {
      me.statusProvider.disconnect();
    }
    me.statusProvider = Ext.direct.Manager.addProvider({
      type: 'polling',
      url: NX.direct.api.POLLING_URLS.coreui_Repository_readStatus,
      interval: 5000,
      baseParams: {
      },
      listeners: {
        data: function(provider, event) {
          if (event.data && event.data.success && event.data.data) {
            me.updateRepositoryModels(event.data.data);
          }
        },
        scope: me
      }
    });
    me.logDebug('Repository status pooling started');
  },

  /**
   * @private
   * Stop polling for repository statuses.
   */
  stopStatusPolling: function() {
    var me = this;

    if (me.statusProvider) {
      me.statusProvider.disconnect();
    }
    me.logDebug('Repository status pooling stopped');
  },

  /**
   * @private
   * Updates Repository store records with values returned by status polling.
   * @param {Array} repositoryStatuses array of status objects
   */
  updateRepositoryModels: function(repositoryStatuses) {
    var me = this;

    Ext.Array.each(repositoryStatuses, function(repositoryStatus) {
      var repositoryModel = me.getNXCoreuiStoreRepositoryStore().findRecord('name', repositoryStatus.repositoryName);
      if (repositoryModel) {
        repositoryModel.set('status', repositoryStatus);
        repositoryModel.commit(true);
      }
    });
  },

  /**
   * Start / Stop status pooling when server is disconnected/connected.
   * @param receiving if we are receiving or not status from server (server connected/disconnected)
   */
  onStateReceivingChanged: function(receiving) {
    var me = this;

    if (me.getList() && receiving) {
      me.startStatusPolling();
    }
    else {
      me.stopStatusPolling();
    }
  },

  updateRepository: function(button) {
    var me = this,
        form = button.up('form'),
        values = form.getValues();

    NX.direct.coreui_Repository.update(values, function(response) {
      if (Ext.isObject(response)) {
        if (response.success) {
          NX.Messages.add({
            text: NX.I18n.format('ADMIN_REPOSITORIES_UPDATE_SUCCESS', response.data.name),
            type: 'success'
          });
          me.loadStoreAndSelect(response.data.id, false);
        }
        else if (Ext.isDefined(response.errors)) {
          form.markInvalid(response.errors);
        }
      }
    });
  },

  createRepository: function(button) {
    var me = this,
        form = button.up('form'),
        values = form.getValues();
    NX.direct.coreui_Repository.create(values, function(response) {
      if (Ext.isObject(response)) {
        if (response.success) {
          NX.Messages.add({
            text: NX.I18n.format('ADMIN_REPOSITORIES_CREATE_SUCCESS', response.data.name),
            type: 'success'
          });
          me.loadStoreAndSelect(response.data.id, false);
        }
        else if (Ext.isDefined(response.errors)) {
          form.markInvalid(response.errors);
        }
      }
    });
  }

});
