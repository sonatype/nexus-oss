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
 * External user "Settings" form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.user.UserSettingsExternalForm', {
  extend: 'NX.view.SettingsForm',
  alias: 'widget.nx-coreui-user-settings-external-form',
  requires: [
    'NX.Conditions',
    'NX.Dialogs'
  ],

  api: {
    submit: 'NX.direct.coreui_User.updateRoleMappings'
  },
  settingsFormSuccessMessage: function(data) {
    return 'User role mappings updated: ' + data['userId'];
  },

  editableMarker: 'You do not have permission to update users or is an not an external user',

  initComponent: function() {
    var me = this;

    me.editableCondition = me.editableCondition || NX.Conditions.and(
        NX.Conditions.isPermitted('security:users', 'update'),
        NX.Conditions.formHasRecord('nx-coreui-user-settings-external-form', function(model) {
          return model.get('external');
        })
    );

    me.items = [
      {
        name: 'userId',
        itemId: 'userId',
        readOnly: true,
        fieldLabel: 'ID',
        helpText: 'The ID assigned to this user, will be used as the username.',
        allowBlank: true
      },
      { name: 'realm', xtype: 'hiddenfield' },
      {
        name: 'firstName',
        fieldLabel: 'First Name',
        helpText: 'The first name of the user.',
        allowBlank: true,
        readOnly: true,
        submitValue: false
      },
      {
        name: 'lastName',
        fieldLabel: 'Last Name',
        helpText: 'The last name of the user.',
        allowBlank: true,
        readOnly: true,
        submitValue: false
      },
      {
        name: 'email',
        fieldLabel: 'Email',
        helpText: 'Email address, to notify user when necessary.',
        allowBlank: true,
        readOnly: true,
        submitValue: false
      },
      {
        xtype: 'combo',
        name: 'status',
        fieldLabel: 'Status',
        helpText: 'The current status of the user.',
        editable: false,
        store: [
          ['active', 'Active'],
          ['disabled', 'Disabled']
        ],
        queryMode: 'local',
        allowBlank: true,
        readOnly: true,
        submitValue: false
      },
      {
        xtype: 'nx-itemselector',
        name: 'roles',
        itemId: 'roles',
        fieldLabel: 'Roles',
        helpText: 'The roles assigned to this user in Nexus.',
        buttons: ['add', 'remove'],
        fromTitle: 'Roles',
        toTitle: 'Given',
        store: 'Role',
        valueField: 'id',
        displayField: 'name',
        delimiter: null,
        allowBlank: true
      },
      {
        xtype: 'textarea',
        name: 'externalRoles',
        itemId: 'externalRoles',
        fieldLabel: 'External Roles',
        helpText: 'Roles externally assigned to user.',
        allowBlank: true,
        readOnly: true,
        submitValue: false
      }
    ];

    me.callParent(arguments);

    Ext.override(me.down('#externalRoles'), {
      /**
       * @override
       * Join external roles using '\n' so they are shown as a role / line.
       */
      setValue: function(value) {
        var formattedValue = value;
        if (Ext.isArray(formattedValue)) {
          formattedValue = [formattedValue.join('\n')];
        }
        this.callParent(formattedValue);
      }
    });

    Ext.override(me.down('#roles'), {
      /**
       * @override
       * Block removal of external roles.
       */
      moveRec: function(add, recs) {
        var externalRoles = me.getRecord().get('externalRoles'),
            canRemove = true;

        if (!add && externalRoles) {
          Ext.Array.each(Ext.Array.from(recs), function(roleModel) {
            if (Ext.Array.contains(externalRoles, roleModel.get('id'))) {
              canRemove = false;
              NX.Dialogs.showInfo(
                  'Cannot remove role',
                  'External mapped role "' + roleModel.get('name')
                      + '" cannot be removed because is assigned to user by external source'
              );
            }
            return canRemove;
          });
        }

        if (canRemove) {
          this.callParent(arguments);
        }
      }
    });
  }

});
