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
 * Repository targets controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.RepositoryTargets', {
  extend: 'NX.controller.MasterDetail',
  requires: [
    'NX.Dialogs',
    'NX.Messages',
    'NX.Permissions'
  ],

  list: 'nx-coreui-repositorytarget-list',

  models: [
    'RepositoryTarget'
  ],
  stores: [
    'RepositoryTarget',
    'RepositoryFormat'
  ],
  views: [
    'repositorytarget.RepositoryTargetAdd',
    'repositorytarget.RepositoryTargetFeature',
    'repositorytarget.RepositoryTargetList',
    'repositorytarget.RepositoryTargetSettings'
  ],
  refs: [
    {
      ref: 'list',
      selector: 'nx-coreui-repositorytarget-list'
    },
    {
      ref: 'info',
      selector: 'nx-coreui-repositorytarget-feature nx-info-panel'
    },
    {
      ref: 'settings',
      selector: 'nx-coreui-repositorytarget-feature nx-coreui-repositorytarget-settings'
    }
  ],
  icons: {
    'target-default': {
      file: 'target.png',
      variants: ['x16', 'x32']
    }
  },
  features: {
    mode: 'admin',
    path: '/Repository/Targets',
    description: 'Manage repository targets',
    view: { xtype: 'nx-coreui-repositorytarget-feature' },
    iconConfig: {
      file: 'target.png',
      variants: ['x16', 'x32']
    },
    visible: function () {
      return NX.Permissions.check('nexus:targets', 'read');
    }
  },
  permission: 'nexus:targets',

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.callParent();

    me.listen({
      store: {
        '#RepositoryFormat': {
          load: me.reselect
        }
      },
      controller: {
        '#Refresh': {
          refresh: me.loadRepositoryFormat
        }
      },
      component: {
        'nx-coreui-repositorytarget-list': {
          beforerender: me.loadRepositoryFormat
        },
        'nx-coreui-repositorytarget-list button[action=new]': {
          click: me.showAddWindow
        },
        'nx-coreui-repositorytarget-settings': {
          submitted: me.onSettingsSubmitted
        }
      }
    });
  },

  /**
   * @override
   */
  getDescription: function (model) {
    return model.get('name');
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
    Ext.widget('nx-coreui-repositorytarget-add');
  },

  /**
   * @private
   * (Re)load repository format store.
   */
  loadRepositoryFormat: function () {
    var me = this,
        list = me.getList();

    if (list) {
      me.getRepositoryFormatStore().load();
    }
  },

  /**
   * @private
   */
  onSettingsSubmitted: function (form, action) {
    var me = this,
        win = form.up('nx-coreui-repositorytarget-add');

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
   * Deletes a repository target.
   * @param {NX.coreui.model.RepositoryTarget} model repository target to be deleted
   */
  deleteModel: function (model) {
    var me = this,
        description = me.getDescription(model);

    NX.direct.coreui_RepositoryTarget.delete_(model.getId(), function (response) {
      me.loadStore();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({ text: 'Target deleted: ' + description, type: 'success' });
      }
    });
  }

});