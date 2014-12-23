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
 * User "Settings" form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.user.UserSettingsForm', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-user-settings-form',
  requires: [
    'NX.Conditions'
  ],

  api: {
    submit: 'NX.direct.coreui_User.update'
  },
  settingsFormSuccessMessage: function(data) {
    return 'User updated: ' + data['userId'];
  },

  editableMarker: 'You do not have permission to update users or is an external user',

  initComponent: function() {
    var me = this;

    me.editableCondition = me.editableCondition || NX.Conditions.and(
        NX.Conditions.isPermitted('security:users', 'update'),
        NX.Conditions.formHasRecord('nx-coreui-user-settings-form', function(model) {
          return !model.get('external');
        })
    );

    me.items = [
      {
        name: 'userId',
        itemId: 'userId',
        readOnly: true,
        fieldLabel: NX.I18n.get('ADMIN_USERS_SETTINGS_ID'),
        helpText: NX.I18n.get('ADMIN_USERS_SETTINGS_ID_HELP'),
        emptyText: NX.I18n.get('ADMIN_USERS_SETTINGS_ID_PLACEHOLDER')
      },
      { name: 'version', xtype: 'hiddenfield' },
      {
        name: 'firstName',
        fieldLabel: NX.I18n.get('ADMIN_USERS_SETTINGS_FIRST'),
        helpText: NX.I18n.get('ADMIN_USERS_SETTINGS_FIRST_HELP'),
        emptyText: NX.I18n.get('ADMIN_USERS_SETTINGS_FIRST_PLACEHOLDER')
      },
      {
        name: 'lastName',
        fieldLabel: NX.I18n.get('ADMIN_USERS_SETTINGS_LAST'),
        helpText: NX.I18n.get('ADMIN_USERS_SETTINGS_LAST_HELP'),
        emptyText: NX.I18n.get('ADMIN_USERS_SETTINGS_LAST_PLACEHOLDER')
      },
      {
        xtype: 'nx-email',
        name: 'email',
        fieldLabel: NX.I18n.get('ADMIN_USERS_SETTINGS_EMAIL'),
        helpText: NX.I18n.get('ADMIN_USERS_SETTINGS_EMAIL_HELP'),
        emptyText: NX.I18n.get('ADMIN_USERS_SETTINGS_EMAIL_PLACEHOLDER')
      },
      {
        xtype: 'combo',
        name: 'status',
        fieldLabel: NX.I18n.get('ADMIN_USERS_SETTINGS_STATUS'),
        helpText: NX.I18n.get('ADMIN_USERS_SETTINGS_STATUS_HELP'),
        emptyText: NX.I18n.get('ADMIN_USERS_SETTINGS_STATUS_PLACEHOLDER'),
        allowBlank: false,
        editable: false,
        store: [
          ['active', NX.I18n.get('ADMIN_USERS_SETTINGS_STATUS_ACTIVE')],
          ['disabled', NX.I18n.get('ADMIN_USERS_SETTINGS_STATUS_DISABLED')]
        ],
        queryMode: 'local'
      },
      {
        xtype: 'nx-itemselector',
        name: 'roles',
        itemId: 'roles',
        fieldLabel: NX.I18n.get('ADMIN_USERS_SETTINGS_ROLES'),
        helpText: NX.I18n.get('ADMIN_USERS_SETTINGS_ROLES_HELP'),
        buttons: ['add', 'remove'],
        fromTitle: NX.I18n.get('ADMIN_USERS_SETTINGS_ROLES_PICKER'),
        toTitle: NX.I18n.get('ADMIN_USERS_SETTINGS_GIVEN_PICKER'),
        store: 'Role',
        valueField: 'id',
        displayField: 'name',
        delimiter: null
      }
    ];

    me.callParent(arguments);
  }

});
