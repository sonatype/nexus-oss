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
 * Repositories controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Repositories', {
  extend: 'NX.controller.MasterDetail',
  requires: [
    'NX.util.Url',
    'NX.Dialogs'
  ],

  list: 'nx-coreui-repository-list',

  models: [
    'Repository'
  ],
  stores: [
    'Repository',
    'RepositoryTemplate'
  ],
  views: [
    'repository.RepositoryAddGroup',
    'repository.RepositoryAddHosted',
    'repository.RepositoryAddHostedMaven',
    'repository.RepositoryAddProxy',
    'repository.RepositoryAddProxyMaven',
    'repository.RepositoryAddVirtual',
    'repository.RepositoryFeature',
    'repository.RepositoryList',
    'repository.RepositorySelectTemplate',
    'repository.RepositorySettingsTab',
    'repository.RepositorySettingsCommon',
    'repository.RepositorySettingsGroup',
    'repository.RepositorySettingsHosted',
    'repository.RepositorySettingsHostedMaven',
    'repository.RepositorySettingsLocalStorage',
    'repository.RepositorySettingsProxy',
    'repository.RepositorySettingsProxyMaven',
    'repository.RepositorySettingsVirtual'
  ],
  refs: [
    { ref: 'list', selector: 'nx-coreui-repository-list' },
    { ref: 'settings', selector: 'nx-coreui-repository-feature nx-coreui-repository-settings-tab' },
    { ref: 'selectTemplate', selector: 'nx-coreui-repository-selecttemplate' },
  ],
  icons: {
    'repository-default': {
      file: 'database.png',
      variants: ['x16', 'x32']
    }
  },
  features: {
    mode: 'admin',
    path: '/Repository/Repositories',
    description: 'Manage repositories',
    view: { xtype: 'nx-coreui-repository-feature' },
    iconConfig: {
      file: 'database.png',
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
          refresh: me.loadRelatedStores
        }
      },
      component: {
        'nx-coreui-repository-list': {
          beforerender: me.loadRelatedStores,
          afterrender: me.startStatusPolling,
          beforedestroy: me.stopStatusPolling
        },
        'nx-coreui-repository-list button[action=browse]': {
          afterrender: me.bindBrowseButton,
          click: me.navigateToBrowseMode
        },
        'nx-coreui-repository-list button[action=new]': {
          click: me.showSelectTemplateWindow
        },
        'nx-coreui-repository-settings': {
          submitted: me.onSettingsSubmitted
        },
        'nx-coreui-repository-selecttemplate grid': {
          selectionchange: me.showAddWindow
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
   * Create & show settings panel for selected repository based on type/provider/format.
   */
  onSelection: function(list, model) {
    var me = this,
        settingsTab, settingsForm;

    if (Ext.isDefined(model)) {
      settingsForm = me.createComponent(
          'settings',
          {
            type: model.get('type').toLowerCase(),
            provider: model.get('provider'),
            format: model.get('format')
          }
      );
      settingsTab = me.getSettings();
      settingsTab.removeAll();
      if (settingsForm) {
        settingsTab.add(settingsForm);
        settingsForm.loadRecord(model);
      }
    }
  },

  /**
   * @private
   * (Re)load repository status / template stores.
   */
  loadRelatedStores: function() {
    var me = this,
        list = me.getList();

    if (list) {
      me.getRepositoryTemplateStore().load();
      NX.direct.coreui_Repository.readStatus(true, function(response) {
        if (Ext.isDefined(response) && response.success) {
          me.updateRepositoryModels(response.data);
        }
      });
    }
  },

  /**
   * @private
   */
  showSelectTemplateWindow: function(menu) {
    Ext.widget('nx-coreui-repository-selecttemplate');
  },

  /**
   * @private
   */
  showAddWindow: function(selectionModel, selected) {
    var me = this;

    me.getSelectTemplate().close();
    me.createComponent('add', Ext.apply({}, selected[0].data));
  },

  /**
   * @private
   * Creates a component specific to action / template.
   */
  createComponent: function(action, template) {
    var me = this,
        cmpName = 'widget.nx-repository-' + action + '-' + template.type + '-' + template.provider,
        cmpClass;

    cmpClass = Ext.ClassManager.getByAlias(cmpName);
    if (!cmpClass) {
      cmpClass = Ext.ClassManager.getByAlias('widget.nx-repository-' + action + '-' + template.type);
    }
    if (cmpClass) {
      return cmpClass.create({ template: template });
    }
    me.logWarn('Could not create component for: ' + cmpName);
    return undefined;
  },

  /**
   * @private
   */
  onSettingsSubmitted: function(form, action) {
    var me = this,
        win = form.up('nx-coreui-repository-add');

    if (win) {
      win.close();
      me.loadStoreAndSelect(action.result.data.id);
    }
    else {
      me.loadStore();
    }
  },

  /**
   * @override
   * Delete repository.
   * @param model repository to be deleted
   */
  deleteModel: function(model) {
    var me = this,
        description = me.getDescription(model);

    NX.direct.coreui_Repository.delete(model.getId(), function(response) {
      me.loadStore();
      if (Ext.isDefined(response) && response.success) {
        NX.Messages.add({
          text: 'Repository deleted: ' + description, type: 'success'
        });
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
    me.statusProvider = Ext.Direct.addProvider({
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
      var repositoryModel = me.getRepositoryStore().getById(repositoryStatus.id);
      if (repositoryModel) {
        repositoryModel.set('localStatus', repositoryStatus['localStatus']);
        repositoryModel.set('proxyMode', repositoryStatus['proxyMode']);
        repositoryModel.set('remoteStatus', repositoryStatus['remoteStatus']);
        repositoryModel.set('remoteStatusReason', repositoryStatus['remoteStatusReason']);
        repositoryModel.commit(true);
      }
    });
  },

  /**
   * @protected
   * Enable 'Browse' when user has selected a repository.
   */
  bindBrowseButton: function(button) {
    var me = this;
    button.mon(
        NX.Conditions.and(
            NX.Conditions.gridHasSelection(me.list)
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
   * Navigate to same repository in browse mode.
   */
  navigateToBrowseMode: function() {
    var me = this,
        list = me.getList();

    NX.Bookmarks.navigateTo(NX.Bookmarks.fromSegments([
      'browse/repository/standard', list.getSelectionModel().getSelection()[0].getId()
    ]));
  }

});