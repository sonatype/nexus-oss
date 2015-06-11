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
 * Role "Settings" form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.role.RoleSettingsForm', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-role-settings-form',
  requires: [
    'NX.Conditions',
    'NX.coreui.store.Role',
    'NX.I18n'
  ],

  api: {
    submit: 'NX.direct.coreui_Role.update'
  },
  settingsFormSuccessMessage: function(data) {
    return NX.I18n.get('ADMIN_ROLES_UPDATE_SUCCESS') + data['name'];
  },

  editableMarker: NX.I18n.get('ADMIN_ROLES_UPDATE_ERROR'),

  initComponent: function() {
    var me = this,
        idField,
        roleStore = Ext.create('NX.coreui.store.Role');

    me.editableCondition = me.editableCondition || NX.Conditions.and(
        NX.Conditions.isPermitted('nexus:roles:update'),
        NX.Conditions.formHasRecord('nx-coreui-role-settings-form', function(model) {
          return !model.get('readOnly');
        })
    );

    roleStore.load();

    if (me.source) {
      idField = {
        xtype: 'combo',
        name: 'id',
        itemId: 'id',
        fieldLabel: NX.I18n.get('ADMIN_ROLES_SETTINGS_ID_MAPPED'),
        emptyText: NX.I18n.get('ADMIN_ROLES_SETTINGS_ID_MAPPED_PLACEHOLDER'),
        editable: false,
        store: 'RoleBySource',
        queryMode: 'local',
        displayField: 'name',
        valueField: 'id'
      };
    }
    else {
      idField = {
        name: 'id',
        itemId: 'id',
        readOnly: true,
        fieldLabel: NX.I18n.get('ADMIN_ROLES_SETTINGS_ID')
      };
    }

    me.items = [
      {
        xtype: 'hiddenfield',
        name: 'version'
      },
      {
        xtype: 'hiddenfield',
        name: 'source',
        value: me.source || 'default'
      },
      idField,
      {
        name: 'name',
        fieldLabel: NX.I18n.get('ADMIN_ROLES_SETTINGS_NAME')
      },
      {
        name: 'description',
        allowBlank: true,
        fieldLabel: NX.I18n.get('ADMIN_ROLES_SETTINGS_DESCRIPTION')
      },
      {
        xtype: 'nx-itemselector',
        name: 'privileges',
        itemId: 'privileges',
        fieldLabel: NX.I18n.get('ADMIN_ROLES_SETTINGS_PRIVILEGES'),
        allowBlank: true,
        buttons: ['add', 'remove'],
        fromTitle: NX.I18n.get('ADMIN_ROLES_SETTINGS_PRIVILEGES_PICKER'),
        toTitle: NX.I18n.get('ADMIN_ROLES_SETTINGS_GIVEN_PICKER'),
        store: 'Privilege',
        valueField: 'id',
        displayField: 'name',
        delimiter: null
      },
      {
        xtype: 'nx-itemselector',
        name: 'roles',
        itemId: 'roles',
        fieldLabel: NX.I18n.get('ADMIN_ROLES_SETTINGS_ROLES'),
        allowBlank: true,
        buttons: ['add', 'remove'],
        fromTitle: NX.I18n.get('ADMIN_ROLES_SETTINGS_ROLES_PICKER'),
        toTitle: NX.I18n.get('ADMIN_ROLES_SETTINGS_CONTAINED_PICKER'),
        store: roleStore,
        valueField: 'id',
        displayField: 'name',
        delimiter: null,
        listeners: {
          /**
           * Ensure that the reference to the Role we're updating is not displayed.
           */
          change: function(roles) {
            var form = roles.up('form'),
                record = form.getRecord(),
                store = roles.getStore();
            if (record) {
              store.clearFilter(true);
              store.filter([
                {
                  filterFn: function(item) {
                    return item.get('id') !== record.get('id'); 
                  }
                }
              ]);
            }
          }
        }
      }
    ];

    me.callParent(arguments);
  }

});
