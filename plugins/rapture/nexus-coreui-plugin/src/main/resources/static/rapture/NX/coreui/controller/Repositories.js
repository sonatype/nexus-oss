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
    { ref: 'selectTemplate', selector: 'nx-coreui-repository-selecttemplate' }
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
          refresh: me.loadRelatedStores
        },
        '#State': {
          receivingchanged: me.onStateReceivingChanged
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
        'nx-coreui-repository-list button[action=more]': {
          afterrender: me.bindMoreButton
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
        moreButton = list.down('button[action=more]'),
        settingsTab, settingsForm;

    if (Ext.isDefined(model)) {
      me.fillMoreButtonMenu(moreButton, model);
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
   * Add menu entries to 'More' button, based on current selected repository.
   * @param {Ext.button.Button} button 'More' button
   * @param {NX.coreui.model.Repository} model repository model
   */
  fillMoreButtonMenu: function(button, model) {
    var me = this,
        type = model.get('type'),
        provider = model.get('provider'),
        localStatus = model.get('localStatus'),
        proxyMode = model.get('proxyMode'),
        userManaged = model.get('userManaged');

    me.removeMenuItem(button, 'expirecache');
    if (NX.Conditions.isPermitted("nexus:cache", "delete")
        && type !== 'virtual' && localStatus === 'IN_SERVICE' && userManaged) {
      button.menu.add({
        text: 'Expire Cache', action: 'expirecache', handler: Ext.bind(me.expireCache, me, [model])
      });
    }

    me.removeMenuItem(button, 'rebuildmetadata');
    if (NX.Conditions.isPermitted("nexus:metadata", "delete")
        && (provider === 'maven2' || provider === 'maven1')
        && (type === 'hosted' || type === 'group') && localStatus === 'IN_SERVICE' && userManaged) {
      button.menu.add({
        text: 'Rebuild Metadata', action: 'rebuildmetadata', handler: Ext.bind(me.rebuildMavenMetadata, me, [model])
      });
    }

    me.removeMenuItem(button, 'blockproxy');
    if (NX.Conditions.isPermitted("nexus:repostatus", "update")
        && type === 'proxy' && proxyMode === 'ALLOW') {
      button.menu.add({
        text: 'Block Proxy', action: 'blockproxy', handler: Ext.bind(me.blockProxy, me, [model])
      });
    }

    me.removeMenuItem(button, 'allowproxy');
    if (NX.Conditions.isPermitted("nexus:repostatus", "update")
        && type === 'proxy' && proxyMode !== 'ALLOW') {
      button.menu.add({
        text: 'Allow Proxy', action: 'allowproxy', handler: Ext.bind(me.allowProxy, me, [model])
      });
    }

    me.removeMenuItem(button, 'putoutofservice');
    if (NX.Conditions.isPermitted("nexus:repostatus", "update")
        && type !== 'group' && localStatus === 'IN_SERVICE') {
      button.menu.add({
        text: 'Put Out of Service', action: 'putoutofservice', handler: Ext.bind(me.putOutOfService, me, [model])
      });
    }

    me.removeMenuItem(button, 'putinservice');
    if (NX.Conditions.isPermitted("nexus:repostatus", "update")
        && type !== 'group' && localStatus !== 'IN_SERVICE') {
      button.menu.add({
        text: 'Put in Service', action: 'putinservice', handler: Ext.bind(me.putInService, me, [model])
      });
    }

    me.removeMenuItem(button, 'repairindex');
    me.removeMenuItem(button, 'updateindex');
    if (NX.Conditions.isPermitted("nexus:index", "delete")
        && (provider === 'maven2' || provider === 'maven1')
        && type !== 'virtual') {
      button.menu.add({
        text: 'Repair Index', action: 'repairindex', handler: Ext.bind(me.repairIndex, me, [model])
      });
      button.menu.add({
        text: 'Update Index', action: 'updateindex', handler: Ext.bind(me.updateIndex, me, [model])
      });
    }
  },

  /**
   * Removes from 'More' button, if present, menu item with specified action.
   * @param {Ext.button.Button} button 'More' button
   * @param actionName menu item action name to be removed
   */
  removeMenuItem: function(button, actionName) {
    var menuItem;

    menuItem = button.down('menuitem[action=' + actionName + ']');
    if (menuItem) {
      button.menu.remove(menuItem);
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
        if (Ext.isObject(response) && response.success) {
          me.updateRepositoryModels(response.data);
        }
      });
    }
  },

  /**
   * @private
   */
  showSelectTemplateWindow: function() {
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

    NX.direct.coreui_Repository.delete_(model.getId(), function(response) {
      me.loadStore();
      if (Ext.isObject(response) && response.success) {
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
   * @protected
   * Enable 'More' when user has selected a repository.
   */
  bindMoreButton: function(button) {
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
      'browse/repository/standard', list.getSelectionModel().getSelection()[0].getId(), '/'
    ]));
  },

  /**
   * Expire cache for repository.
   * @param {NX.coreui.model.Repository} model repository model
   */
  expireCache: function(model) {
    NX.direct.coreui_Repository.clearCache(model.getId(), '/', function(response) {
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: 'Started expiring caches of "' + model.get('name'),
          type: 'success'
        });
      }
    });
  },

  /**
   * Rebuild metadata for repository.
   * @param {NX.coreui.model.Repository} model repository model
   */
  rebuildMavenMetadata: function(model) {
    NX.direct.coreui_Maven.rebuildMetadata(model.getId(), '/', function(response) {
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: 'Started rebuilding metadata of "' + model.get('name'),
          type: 'success'
        });
      }
    });
  },

  /**
   * Block proxy-ing for selected repository.
   * @param {NX.coreui.model.Repository} model repository model
   */
  blockProxy: function(model) {
    var me = this;

    NX.direct.coreui_Repository.updateStatus(model.getId(), undefined, 'BLOCKED_MANUAL', function(response) {
      if (Ext.isObject(response) && response.success) {
        me.loadStore();
        NX.Messages.add({
          text: 'Blocked proxy on "' + model.get('name'),
          type: 'success'
        });
      }
    });
  },

  /**
   * Allow proxy-ing for selected repository.
   * @param {NX.coreui.model.Repository} model repository model
   */
  allowProxy: function(model) {
    var me = this;

    NX.direct.coreui_Repository.updateStatus(model.getId(), undefined, 'ALLOW', function(response) {
      if (Ext.isObject(response) && response.success) {
        me.loadStore();
        NX.Messages.add({
          text: 'Allowed proxy on "' + model.get('name'),
          type: 'success'
        });
      }
    });
  },

  /**
   * Put selected repository out of service.
   * @param {NX.coreui.model.Repository} model repository model
   */
  putOutOfService: function(model) {
    var me = this;

    NX.direct.coreui_Repository.updateStatus(model.getId(), 'OUT_OF_SERVICE', undefined, function(response) {
      if (Ext.isObject(response) && response.success) {
        me.loadStore();
        NX.Messages.add({
          text: 'Repository "' + model.get('name') + ' was put out of service',
          type: 'success'
        });
      }
    });
  },

  /**
   * Put selected repository in service.
   * @param {NX.coreui.model.Repository} model repository model
   */
  putInService: function(model) {
    var me = this;

    NX.direct.coreui_Repository.updateStatus(model.getId(), 'IN_SERVICE', undefined, function(response) {
      if (Ext.isObject(response) && response.success) {
        me.loadStore();
        NX.Messages.add({
          text: 'Repository "' + model.get('name') + ' was put in service',
          type: 'success'
        });
      }
    });
  },

  /**
   * Repair index for selected repository.
   * @param {NX.coreui.model.Repository} model repository model
   */
  repairIndex: function(model) {
    NX.direct.indexerLucene_Index.repair(model.getId(), '/', function(response) {
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: 'Started repairing index of "' + model.get('name'),
          type: 'success'
        });
      }
    });
  },

  /**
   * Update index for selected repository.
   * @param {NX.coreui.model.Repository} model repository model
   */
  updateIndex: function(model) {
    NX.direct.indexerLucene_Index.update(model.getId(), '/', function(response) {
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: 'Started updating index of "' + model.get('name'),
          type: 'success'
        });
      }
    });
  }

});