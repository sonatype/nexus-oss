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
 * Role settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.role.RoleSettings', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-role-settings',

  api: {
    submit: 'NX.direct.coreui_Role.update'
  },
  settingsFormSuccessMessage: function (data) {
    return 'Role updated: ' + data['name'];
  },
  editableCondition: NX.Conditions.and(
      NX.Conditions.isPermitted('security:roles', 'update'),
      NX.Conditions.formHasRecord('nx-coreui-role-settings', function (model) {
        return !model.get('readOnly');
      })
  ),
  editableMarker: 'You do not have permission to update roles or role is readonly',

  initComponent: function () {
    var me = this,
        idField,
        roleStore = Ext.create('NX.coreui.store.Role');

    roleStore.load();

    if (me.source) {
      idField = {
        xtype: 'combo',
        name: 'id',
        itemId: 'id',
        fieldLabel: 'Mapped Role',
        emptyText: 'select a role',
        editable: false,
        store: 'RoleBySource',
        queryMode: 'local',
        displayField: 'name',
        valueField: 'id'
      }
    }
    else {
      idField = {
        name: 'id',
        itemId: 'id',
        readOnly: true,
        fieldLabel: 'Role Id',
        emptyText: 'enter a role id'
      };
    }

    me.items = [
      {
        xtype: 'hiddenfield',
        name: 'source',
        value: me.source || 'default'
      },
      idField,
      {
        name: 'name',
        fieldLabel: 'Name',
        emptyText: 'enter a name'
      },
      {
        name: 'description',
        allowBlank: true,
        fieldLabel: 'Description',
        emptyText: 'enter a description'
      },
      {
        xtype: 'nx-itemselector',
        name: 'privileges',
        itemId: 'privileges',
        allowBlank: true,
        buttons: ['add', 'remove'],
        fromTitle: 'Privileges',
        toTitle: 'Given',
        store: 'Privilege',
        valueField: 'id',
        displayField: 'name',
        delimiter: null
      },
      {
        xtype: 'nx-itemselector',
        name: 'roles',
        itemId: 'roles',
        allowBlank: true,
        buttons: ['add', 'remove'],
        fromTitle: 'Roles',
        toTitle: 'Contained',
        store: roleStore,
        valueField: 'id',
        displayField: 'name',
        delimiter: null
      }
    ];

    me.callParent(arguments);
  }

});
