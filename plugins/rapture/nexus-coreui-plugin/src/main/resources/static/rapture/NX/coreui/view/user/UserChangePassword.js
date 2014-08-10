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
 * Chnage password window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.user.UserChangePassword', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-user-changepassword',
  requires: [
    'NX.Conditions'
  ],

  title: 'Change Password',
  defaultFocus: 'password',

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = {
      xtype: 'nx-settingsform',
      editableCondition: NX.Conditions.isPermitted('security:userschangepw', 'create'),
      editableMarker: 'You do not have permission to change your password',

      items: [
        {
          xtype: 'nx-password',
          name: 'password',
          itemId: 'password',
          fieldLabel: 'Password',
          helpText: 'The new password required to you into the system.',
          emptyText: 'enter a password'
        },
        {
          xtype: 'nx-password',
          allowBlank: true,
          fieldLabel: 'Confirm Password',
          helpText: 'Re-enter the password to validate entry.',
          emptyText: 'confirm above password',
          submitValue: false,
          validator: function () {
            var me = this;
            return (me.up('form').down('#password').getValue() === me.getValue()) ? true : 'Passwords do not match';
          }
        }
      ],

      buttons: [
        { text: 'Change password', action: 'changepassword', formBind: true, ui: 'primary' },
        { text: 'Cancel', handler: function () {
          this.up('window').close();
        }}
      ]
    };

    me.callParent();
  }
});
