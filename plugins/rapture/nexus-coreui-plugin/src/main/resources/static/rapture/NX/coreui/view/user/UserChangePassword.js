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
 * Chnage password window.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.user.UserChangePassword', {
  extend: 'NX.view.AddWindow',
  alias: 'widget.nx-coreui-user-changepassword',
  requires: [
    'NX.Conditions',
    'NX.I18n'
  ],

  title: NX.I18n.get('ADMIN_USERS_PASSWORD_TITLE'),
  defaultFocus: 'password',

  /**
   * @cfg userId to change password for
   */
  userId: undefined,

  /**
   * @override
   */
  initComponent: function () {
    var me = this;

    me.items = {
      xtype: 'nx-settingsform',
      editableCondition: NX.Conditions.isPermitted('security:userschangepw', 'create'),
      editableMarker: NX.I18n.get('ADMIN_USERS_PASSWORD_ERROR'),

      items: [
        {
          xtype: 'nx-password',
          name: 'password',
          itemId: 'password',
          fieldLabel: NX.I18n.get('ADMIN_USERS_PASSWORD_NEW'),
        },
        {
          xtype: 'nx-password',
          fieldLabel: NX.I18n.get('ADMIN_USERS_PASSWORD_CONFIRM'),
          submitValue: false,
          validator: function () {
            var me = this;
            return (me.up('form').down('#password').getValue() === me.getValue()) ? true : NX.I18n.get('ADMIN_USERS_CREATE_NO_MATCH');
          }
        }
      ],

      buttons: [
        { text: NX.I18n.get('ADMIN_USERS_PASSWORD_SUBMIT_BUTTON'), action: 'changepassword', formBind: true, ui: 'nx-primary' },
        { text: NX.I18n.get('ADMIN_USERS_PASSWORD_CANCEL_BUTTON'), handler: function () {
          this.up('window').close();
        }}
      ]
    };

    me.callParent();
  }
});
