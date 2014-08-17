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
 * Users controller.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.controller.Users', {
  extend: 'NX.controller.MasterDetail',
  requires: [
    'NX.State',
    'NX.Permissions',
    'NX.Security',
    'NX.Icons',
    'NX.Messages'
  ],

  list: 'nx-coreui-user-list',

  models: [
    'User'
  ],
  stores: [
    'User',
    'UserSource',
    'Role'

  ],
  views: [
    'user.UserAccount',
    'user.UserAdd',
    'user.UserChangePassword',
    'user.UserFeature',
    'user.UserList',
    'user.UserSettings'
  ],
  refs: [
    {
      ref: 'list',
      selector: 'nx-coreui-user-list'
    },
    {
      ref: 'settings',
      selector: 'nx-coreui-user-feature nx-coreui-user-settings'
    },
    {
      ref: 'privilegeTrace',
      selector: 'nx-coreui-user-feature nx-coreui-privilege-trace'
    },
    {
      ref: 'roleTree',
      selector: 'nx-coreui-user-feature nx-coreui-role-tree'
    }
  ],
  icons: {
    'user-default': {
      file: 'user.png',
      variants: ['x16', 'x32']
    },
    'default-security-source': {
      file: 'user.png',
      variants: ['x16']
    },
    'allconfigured-security-source': {
      file: 'user.png',
      variants: ['x16']
    }
  },
  features: [
    {
      mode: 'admin',
      path: '/Security/Users',
      description: 'Manage users',
      view: { xtype: 'nx-coreui-user-feature' },
      iconConfig: {
        file: 'group.png',
        variants: ['x16', 'x32']
      },
      visible: function () {
        return NX.Permissions.check('security:users', 'read');
      },
      weight: 30
    },
    {
      mode: 'user',
      path: '/Account',
      description: 'Manage your account',
      view: { xtype: 'nx-coreui-user-account' },
      iconConfig: {
        file: 'user.png',
        variants: ['x16', 'x32']
      },
      visible: function () {
        return NX.Security.hasUser();
      }
    }
  ],
  permission: 'security:users',

  /**
   * @override
   */
  init: function () {
    var me = this;

    me.callParent();

    me.listen({
      controller: {
        '#Refresh': {
          refresh: me.loadRelatedStores
        }
      },
      store: {
        '#UserSource': {
          load: me.onUserSourceLoad
        }
      },
      component: {
        'nx-coreui-user-list': {
          beforerender: me.loadRelatedStores
        },
        'nx-coreui-user-list button[action=new]': {
          click: me.showAddWindow
        },
        'nx-coreui-user-settings': {
          submitted: me.onSettingsSubmitted
        },
        'nx-coreui-user-list menuitem[action=filter]': {
          click: me.filterBySource
        },
        'nx-coreui-user-account button[action=changepassword]': {
          click: me.showChangePasswordWindow,
          afterrender: me.bindChangePasswordButton
        },
        'nx-coreui-user-changepassword button[action=changepassword]': {
          click: me.changePassword
        },
        'nx-coreui-user-feature nx-coreui-privilege-trace': {
          activate: me.onPrivilegeTraceActivate
        },
        'nx-coreui-user-feature nx-coreui-role-tree': {
          activate: me.onRoleTreeActivate
        }
      }
    });
  },

  /**
   * @override
   */
  getDescription: function (model) {
    return model.get('firstName') + ' ' + model.get('lastName');
  },

  /**
   * @override
   */
  onSelection: function (list, model) {
    var me = this;

    if (Ext.isDefined(model)) {
      me.getSettings().loadRecord(model);
      me.getPrivilegeTrace().loadRecord(model.data);
      me.getRoleTree().loadRecord(model.data);
    }
  },

  /**
   * @private
   */
  showAddWindow: function () {
    Ext.widget('nx-coreui-user-add');
  },

  /**
   * @private
   */
  showChangePasswordWindow: function () {
    NX.Security.doWithAuthenticationToken(
        'Changing password requires validation of your credentials.',
        {
          success: function (authToken) {
            Ext.widget('nx-coreui-user-changepassword', { authToken: authToken });
          }
        }
    );
  },

  /**
   * @private
   * Load store for selected source.
   */
  loadStore: function () {
    var me = this,
        list = me.getList(),
        userSourceButton;

    if (list) {
      userSourceButton = list.down('button[action=filter]');
      if (!userSourceButton.sourceId) {
        userSourceButton.sourceId = 'default';
      }
      me.getUserStore().load({
        params: {
          filter: [
            {
              property: 'source', value: userSourceButton.sourceId
            }
          ]
        }
      });
    }
  },

  /**
   * @private
   * (Re)load related stores.
   */
  loadRelatedStores: function () {
    var me = this,
        list = me.getList();

    if (list) {
      me.getUserSourceStore().load();
      me.getRoleStore().load();
    }
  },

  /**
   * @private
   * (Re)create user source filters.
   */
  onUserSourceLoad: function (store) {
    var me = this,
        list = me.getList(),
        userSourceButton;

    if (list) {
      userSourceButton = list.down('button[action=filter]');
      if (userSourceButton.menu.items.length > 1) {
        userSourceButton.menu.removeAll();
      }
      if (!userSourceButton.sourceId) {
        userSourceButton.sourceId = 'default';
      }
      store.each(function (source) {
        var iconCls = NX.Icons.cls(source.getId().toLowerCase() + '-security-source', 'x16');
        userSourceButton.menu.add({
          text: source.get('name'),
          iconCls: iconCls,
          group: 'usersource',
          checked: userSourceButton.sourceId === source.getId(),
          action: 'filter',
          source: source
        });
        if (userSourceButton.sourceId === source.getId()) {
          userSourceButton.setText(source.get('name'));
          userSourceButton.setIconCls(iconCls);
        }
      });
    }
  },

  /**
   * @private
   */
  filterBySource: function (menuItem) {
    var me = this,
        userSourceButton = me.getList().down('button[action=filter]');

    userSourceButton.setText(menuItem.source.get('name'));
    userSourceButton.setIconCls(menuItem.iconCls);
    userSourceButton.sourceId = menuItem.source.getId();

    me.loadStore();
  },

  /**
   * @private
   */
  onRoleTreeActivate: function (panel) {
    var me = this;

    panel.loadRecord({
      roles: me.getSettings().down('#roles').getValue()
    });
  },

  /**
   * @private
   */
  onPrivilegeTraceActivate: function (panel) {
    var me = this;

    panel.loadRecord({
      roles: me.getSettings().down('#roles').getValue()
    });
  },

  /**
   * @private
   */
  onSettingsSubmitted: function (form, action) {
    var me = this,
        win = form.up('nx-coreui-user-add');

    if (win) {
      win.close();
      me.loadStoreAndSelect(action.result.data.id);
    }
    else {
      me.loadStore();
    }
  },

  /**
   * @protected
   * Enable 'Delete' when user has 'delete' permission and user is not the current logged in used or the anonymous user.
   */
  bindDeleteButton: function (button) {
    var me = this;
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted(me.permission, 'delete'),
            NX.Conditions.gridHasSelection(me.list, function (model) {
              var userId = model.getId();
              return (userId !== NX.State.getUser().id) && (userId !== NX.State.getValue('anonymousUsername'));
            })
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
   * @override
   * Deletes a user.
   * @param model user to be deleted
   */
  deleteModel: function (model) {
    var me = this,
        description = me.getDescription(model);

    NX.direct.coreui_User.delete_(model.getId(), model.get('realm'), function (response) {
      me.loadStore();
      if (Ext.isObject(response) && response.success) {
        NX.Messages.add({
          text: 'User deleted: ' + description, type: 'success'
        });
      }
    });
  },

  /**
   * @override
   * @private
   * Enable 'Change Password' when user has 'security:userschangepw:create' permission.
   */
  bindChangePasswordButton: function (button) {
    button.mon(
        NX.Conditions.and(
            NX.Conditions.isPermitted('security:userschangepw', 'create')
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
   */
  changePassword: function (button) {
    var win = button.up('window'),
        password = button.up('form').down('#password').getValue();

    NX.direct.coreui_User.changePassword(win.authToken, password, function (response) {
      if (Ext.isObject(response) && response.success) {
        win.close();
        NX.Messages.add({ text: 'Password changed', type: 'success' });
      }
    });
  }

});