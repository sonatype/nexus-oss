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
 * User settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.user.UserSettings', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-user-settings',
  requires: [
    'NX.Conditions'
  ],

  api: {
    submit: 'NX.direct.coreui_User.update'
  },
  settingsFormSuccessMessage: function (data) {
    return 'User updated: ' + data['id'];
  },

  editableMarker: 'You do not have permission to update users',

  initComponent: function () {
    var me = this;

    me.editableCondition = NX.Conditions.isPermitted('security:users', 'update');

    me.items = [
      {
        name: 'id',
        itemId: 'id',
        readOnly: true,
        fieldLabel: 'ID',
        helpText: 'The ID assigned to this user, will be used as the username.',
        emptyText: 'enter a user id'
      },
      {
        name: 'firstName',
        fieldLabel: 'First Name',
        helpText: 'The first name of the user.',
        emptyText: 'enter first name'
      },
      {
        name: 'lastName',
        fieldLabel: 'Last Name',
        helpText: 'The last name of the user.',
        emptyText: 'enter last name'
      },
      {
        xtype: 'nx-email',
        name: 'email',
        fieldLabel: 'Email',
        helpText: 'Email address, to notify user when necessary.',
        emptyText: 'enter an email address'
      },
      {
        xtype: 'combo',
        name: 'status',
        fieldLabel: 'Status',
        helpText: 'The current status of the user.',
        emptyText: 'select status',
        allowBlank: false,
        editable: false,
        store: [
          ['active', 'Active'],
          ['disabled', 'Disabled']
        ],
        queryMode: 'local'
      },
      {
        xtype: 'nx-itemselector',
        name: 'roles',
        itemId: 'roles',
        fieldLabel: 'Roles',
        helpText: 'The roles assigned to this user.',
        buttons: ['add', 'remove'],
        fromTitle: 'Roles',
        toTitle: 'Given',
        store: 'Role',
        valueField: 'id',
        displayField: 'name',
        delimiter: null
      }
    ];

    me.callParent(arguments);
  }

});
