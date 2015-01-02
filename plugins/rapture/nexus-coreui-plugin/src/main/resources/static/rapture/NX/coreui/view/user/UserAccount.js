/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2015 Sonatype, Inc.
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
 * User account settings form.
 *
 * @since 3.0
 */
Ext.define('NX.coreui.view.user.UserAccount', {
  extend: 'NX.view.SettingsPanel',
  alias: 'widget.nx-coreui-user-account',

  initComponent: function () {
    var me = this;

    me.items = [
      {
        xtype: 'nx-settingsform',
        settingsFormSuccessMessage: 'User account settings $action',
        api: {
          load: 'NX.direct.coreui_User.readAccount',
          submit: 'NX.direct.coreui_User.updateAccount'
        },
        items: [
          {
            xtype: 'label',
            html: NX.I18n.get('USER_ACCOUNT_HELP')
          },
          {
            name: 'userId',
            itemId: 'userId',
            readOnly: true,
            fieldLabel: NX.I18n.get('USER_ACCOUNT_ID'),
            helpText: NX.I18n.get('USER_ACCOUNT_ID_HELP'),
            emptyText: NX.I18n.get('USER_ACCOUNT_ID_PLACEHOLDER')
          },
          {
            name: 'firstName',
            fieldLabel: NX.I18n.get('USER_ACCOUNT_FIRST'),
            helpText: NX.I18n.get('USER_ACCOUNT_FIRST_HELP'),
            emptyText: NX.I18n.get('USER_ACCOUNT_FIRST_PLACEHOLDER')
          },
          {
            name: 'lastName',
            fieldLabel: NX.I18n.get('USER_ACCOUNT_LAST'),
            helpText: NX.I18n.get('USER_ACCOUNT_LAST_HELP'),
            emptyText: NX.I18n.get('USER_ACCOUNT_LAST_PLACEHOLDER')
          },
          {
            xtype: 'nx-email',
            name: 'email',
            fieldLabel: NX.I18n.get('USER_ACCOUNT_EMAIL'),
            helpText: NX.I18n.get('USER_ACCOUNT_EMAIL_HELP'),
            emptyText: NX.I18n.get('USER_ACCOUNT_EMAIL_PLACEHOLDER')
          }
        ]
      }
    ];

    me.callParent(arguments);

    me.items.get(0).getDockedItems('toolbar[dock="bottom"]')[0].add({
      xtype: 'button', text: NX.I18n.get('USER_ACCOUNT_PASSWORD_BUTTON'), action: 'changepassword', ui: 'danger', glyph: 'xf023@FontAwesome' /* fa-lock */, disabled: true
    });
  }

});
